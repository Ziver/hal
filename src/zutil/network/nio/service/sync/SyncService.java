package zutil.network.nio.service.sync;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

import zutil.MultiPrintStream;
import zutil.network.nio.NioNetwork;
import zutil.network.nio.message.Message;
import zutil.network.nio.message.SyncMessage;
import zutil.network.nio.service.NetworkService;

public class SyncService extends NetworkService{
	// list of objects to sync	
	private HashMap<String, ObjectSync> sync;

	public SyncService(NioNetwork nio){
		super(nio);
		sync = new HashMap<String, ObjectSync>();
	}

	/**
	 * Adds a SyncObject to the sync list
	 * @param os The object to sync
	 */
	public void addSyncObject(ObjectSync os){
		sync.put(os.id, os);
		if(NioNetwork.DEBUG>=1)MultiPrintStream.out.println("New Sync object: "+os);
	}

	public void handleMessage(Message message, SocketChannel socket){
		if(message instanceof SyncMessage){
			SyncMessage syncMessage = (SyncMessage)message;
			if(syncMessage.type == SyncMessage.MessageType.SYNC){
				ObjectSync obj = sync.get(syncMessage.id);
				if(obj != null){
					if(NioNetwork.DEBUG>=2)MultiPrintStream.out.println("Syncing Message...");
					obj.syncObject(syncMessage);
				}
			}
			else if(syncMessage.type == SyncMessage.MessageType.REMOVE){
				sync.remove(syncMessage.id).remove();
			}
		}
	}

	/**
	 * Syncs all the objects whit the server
	 */
	public void sync(){
		for(String id : sync.keySet()){
			sync.get(id).sendSync();
		}
	}
	
	public static SyncService getInstance(){
		return (SyncService)instance;
	}
}
