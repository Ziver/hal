import {eventStore} from 'EventStore'
import EventTableRow from 'EventTableRowComponent'

export default {
    data() {
        return {
          eventStore,
        }
    },
    components: {
        EventTableRow,
    },
    mounted() {
        eventStore.enableAutoLoad(true);
    },
    unmounted() {
        eventStore.enableAutoLoad(false);
    },
    template: `
        <table id="event-device-table2" class="table table-hover table-condensed">
            <thead>
                <tr>
                    <th class="col-md-4">Name</th>
                    <th class="col-md-3">Type</th>
                    <th class="col-md-1">Data</th>
                    <th class="col-md-2">Last Update</th>
                    <th class="col-md-2 text-right">Actions</th>
                </tr>

                <EventTableRow v-for="e in eventStore.events" :key="e.id" :id="e.id" />
            </thead>
        </table>
    `
}