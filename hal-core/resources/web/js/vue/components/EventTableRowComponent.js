import {eventStore} from 'EventStore'
import EventAction from 'EventActionComponent'

export default {
  props: {
    'id': {default: 0},
  },
  data() {
  let event = eventStore.getEvent(this.id)
    return {
        event: event,
        timestamp: getRelTimestamp(event.data?.timestamp)
    }
  },
  components: {
    EventAction
  },
  template: `
    <tr :data-device-id="event.id">
        <td><a :href="'event/' + event.id">{{ event.name }}</a></td>
        <td>{{ event.configType }}</td>
        <td>{{ event.data?.valueStr }}</td>
        <td class="timestamp">{{ timestamp }}</td>
        <td>
            <EventAction :id="event.id"/>
        </td>
    </tr>
    `
}