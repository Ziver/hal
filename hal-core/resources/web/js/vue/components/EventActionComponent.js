import {eventStore} from 'EventStore'

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
    template: `
        <form method="POST">
            <input type="hidden" name="action" value="modify">
            <input type="hidden" name="action-id" :value="event.id">

            <div class="btn-toolbar pull-right">
                <template v-if="event.dataType === null">
                </template>
                <template v-else-if="event.dataType === 'ColorEventData'">
                    <input type="hidden" name="type" value="color">
                    <input type="color" name="data" onchange="this.form.submit()" :value="event.data?.valueStr">
                </template>
                <template v-else-if="event.dataType === 'LevelEventData'">
                    <input type="hidden" name="type" value="level">
                    <input class="styled-slider slider-progress" type="range" name="data" min="0" max="100" step="10" onchange="this.form.submit()" :value="event.data?.value * 100">
                </template>
                <template v-else-if="event.dataType === 'OnOffEventData'">
                    <input type="hidden" name="type" value="on-off">
                    <input class="switch" type="checkbox" name="data" onchange="this.form.submit()" v-model="checkboxChecked">
                </template>
            </div>
        </form>
    `
}