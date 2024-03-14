import {eventStore} from 'EventStore'

export default {
    data() {
        var id = this.$route.params.id;
        return {
            id:    id,
            event: eventStore.getEvent(id),
        }
    },
    template: `
        <h1 class="page-header">Details for <a href="?id={{event.id}}">{{event.name}}</a></h1>

        <div class="col-md-5">
            <div class="panel panel-default drop-shadow">
                <div class="panel-heading">Configuration</div>
                <div class="panel-body">
                    <table class="table table-hover table-condensed">
                        <thead>
                        <tr>
                            <th class="text-right">Event ID:</th>
                            <th>{{event.id}</th>
                        </tr>
                        <tr>
                            <th class="text-right">Name:</th>
                            <th>{{event.name}}</th>
                        </tr>
                        </thead>
                        <tr>
                            <th class="text-right">Type:</th>
                            <td>{{event.configType}}</td>
                        </tr>
                        <tr>
                            <th class="text-right">Owner:</th>
                            <td>{{event.owner}} <p></td>
                        </tr>
                        <tr>
                            <th class="text-right">State:</th>
                            <td>
                                <form method="POST">
                                    <input type="hidden" name="action" value="modify">
                                    <input type="hidden" name="action-id" value="{{event.id}}">

                                    <div class="btn-toolbar pull-left">
                                        <input class="toggle-switch" type="checkbox" name="enabled"
                                               data-on-color="danger">
                                    </div>
                                </form>
                            </td>
                        </tr>

                        <tr v-for="(confName, confValue) in event.config">
                            <th class="text-right">{{name}}:</th>
                            <td>{{confValue}}</td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>

        <div class="col-md-7">
            <div class="panel panel-default drop-shadow">
                <div class="panel-heading">History data</div>
                <div class="panel-body">
                    <table class="table table-hover table-condensed">
                        <thead>
                            <th class="col-md-6">Timestamp</th>
                            <th class="col-md-2">Raw Data</th>
                        </thead>

                        <tr>
                            <td><span class="timestamp">{{event.data.timestamp}}</span></td>
                            <td>{{event.data.value}}</td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>

        <script>
            $(function (){
                $(".toggle-switch").on("switchChange.bootstrapSwitch", function (event, state) {
                    $(this).closest('form').submit();
                });
            });
        </script>
    `
}