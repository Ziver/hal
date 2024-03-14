import { reactive } from 'vue'
import { alertStore } from 'AlertStore'

export const eventStore = reactive({
    events: [],
    pollTimerId: null,
    pollInterval: 10000, // 10 sec

    enableAutoLoad(enabled = true) {
        if (enabled) {
            if (this.pollTimerId !== null)
                return; // Timer already initialized
            eventStore.load();

            this.pollTimerId = setInterval(function() {
                eventStore.load();
            }, this.pollInterval);
        } else {
            clearInterval(this.pollTimerId);
            this.pollTimerId == null;
        }
    },

    load() {
        alertStore.setLoading(true);

        fetch('/api/event', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
            },
        })
        .then(response => response.json())
        .then(json => {
            if (json['error'] != null) {
                alertStore.alertError(json['error']);
                return;
            }

            eventStore.events = json;
            alertStore.setLoading(false);
        })
    },

    getEvent(id) {
        let event = eventStore.events.find(event => id == event.id);
        return event;
    }
});