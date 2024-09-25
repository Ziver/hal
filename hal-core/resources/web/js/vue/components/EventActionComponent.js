import {eventStore} from 'EventStore'
import { alertStore } from 'AlertStore'

export default {
    props: {
        'id': {default: 0},
    },
    data() {
        let internalEvent = eventStore.getEvent(this.id);
        return {
            event: internalEvent,
            checkboxChecked: internalEvent.data?.valueStr == 'ON',
        }
    },
    methods: {
        changeState(changeEvent) {
            fetch('/api/event?' + new URLSearchParams({
                  id: this.event.id,
                  action: "modify",
                  data: changeEvent.target.type == "checkbox" ? this.checkboxChecked : changeEvent.target.value
              }).toString(), {
                method: 'PUT',
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
            })
        }
    },
    template: `
        <div class="btn-toolbar pull-right">
            <template v-if="event.dataType === 'ColorEventData'">
                <input type="color" @change="changeState" :value="event.data?.valueStr">
            </template>
            <template v-else-if="event.dataType === 'LevelEventData'">
                <input class="styled-slider slider-progress" type="range" min="0.0" max="1.0" step="0.1" @change="changeState" :value="event.data?.value">
            </template>
            <template v-else-if="event.dataType === 'OnOffEventData'">
                <input class="switch" type="checkbox" @change="changeState" v-model="checkboxChecked">
            </template>
        </div>
    `
}