import { reactive } from 'vue'
import { alertStore } from 'AlertStore'

export const sensorStore = reactive({
    sensors: {},
    pollTimerId: null,
    pollInterval: 10000, // 10 sec

    enableAutoLoad(enabled = true) {
        if (enabled) {
            if (this.pollTimerId !== null)
                return; // Timer already initialized
            sensorStore.load();

            this.pollTimerId = setInterval(function() {
                sensorStore.load();
            }, this.pollInterval);
        } else {
            clearInterval(this.pollTimerId);
            this.pollTimerId == null;
        }
    },

    load() {
        alertStore.setLoading(true);

        fetch('/api/sensor', {
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

            sensorStore.sensors = json;
            alertStore.setLoading(false);
        })
    },
});