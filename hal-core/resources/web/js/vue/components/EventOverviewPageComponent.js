import EventTable from 'EventTableComponent'

export default {
    props: {
    },
    components: {
        EventTable,
    },
    template: `
        <h1 class="page-header">Event Overview</h1>

        <div class="col-md-12">
            <div class="panel panel-default drop-shadow">
                <div class="panel-heading">Local Events</div>
                <div class="panel-body">
                    <EventTable />
                </div>
            </div>
        </div>
    `
}