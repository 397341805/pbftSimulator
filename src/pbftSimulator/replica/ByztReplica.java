package pbftSimulator.replica;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import pbftSimulator.Message;
import pbftSimulator.Settings;
import pbftSimulator.Status;

public class ByztReplica extends Replica{
	//���������Ϊ�˷�ֹ������ѭ��������byzantine�ڵ����޴����ع㲥��Ϣ
	private Map<String, Integer> cntMap;
	
	public ByztReplica(int id, boolean isByzt, int[] netDlys) {
		super(id, isByzt, netDlys);
		receiveTag = Settings.btztReceiveTag;
		sendTag = Settings.btztSendTag;
		processTag = Settings.btztProcessTag;
		cntMap = new HashMap<>();
	}
	
	//��ռͥ�ڵ㲻�����ڵ��ʱ����preprepare����
	//�����ڵ��ʱ���Ͳ���ȷ��sequence
	public void requestProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		if(id != msg.getPriId()) {
			broadcastByztMsg(Message.Preprepare, msg, msgQue, msg.getSeqId(), id, procTime);	
		}else {
			broadcastByztMsg(Message.Preprepare, msg, msgQue, msg.getSeqId() + 1, id, procTime);
		}
	}
	
	//û�дﵽ�����������Ҳ����commit��Ϣ��ֻҪ�յ�prepareRequest�ͷ���commit��Ϣ��
	public void prepareProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		if(remainCnt(msg) > 0) {
			broadcastMsg(Message.Commit, msg, msgQue, procTime);
		}
	}
	
	//�յ�Commit��Ϣʲô������
	public void commitProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
	}
	
	public void timeOutProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
		if(remainCnt(msg) > 0) {
			broadcastMsg(Message.ViewChg, msg, msgQue, msg.getTimestamp());
		}
	}
	
	//û�дﵽ��������������ͷ���NewView��0�ڵ�
	public void viewChangeProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		sendOneMsg(Message.NewView, msg, msgQue, 0, procTime);
		sendTimeOutMsg(msg, msgQue, id);
	}
	
	//�յ��κ�newView����Ϣ���͹㲥prepare��Ϣ
	public void newViewProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		if(remainCnt(msg) > 0) {
			broadcastMsg(Message.Preprepare, msg, msgQue, procTime);
		}
	}
	
	//����Ϣ�����Ϲ��Լ��
	public boolean preCheckMsg(Message msg) {
		String key = msg.getCtx();
		//������г�ʵ�ڵ㶼�Ѿ�confirmed�ˣ���û�м��������������
		if(Settings.remainConfirms <= 0) {
			return false;
		}
		//����ÿ����Ϣ�����㲥��������ֹ������ѭ��
		if(!cntMap.containsKey(key)) {
			cntMap.put(key, 50 * Settings.N);
		}
		if(!statMap.containsKey(key)) {
			statMap.put(key, new Status(Status.Byzantine, 0, 0, 0L));
		} 
		return true;
	}
	
	public void broadcastByztMsg(int type, Message msg, Queue<Message> msgQue, int seqId, int priId, long procTime) {
		for(int i = 0; i < Settings.N; ++i) {
			Message newMsg = new Message(type, msg.getCtx(), id, i, 
					seqId, priId, procTime + (long)netDlys[i]);
			msgQue.add(newMsg);
			newMsg.print(sendTag);
		}
	}
	
	public void sendOneMsg(int type, Message msg, Queue<Message> msgQue, int rcvId, long procTime) {
		Message newMsg = new Message(type, msg.getCtx(), id, rcvId, 
				msg.getSeqId(), msg.getPriId(), 
				procTime + (long)netDlys[rcvId]);
		msgQue.add(newMsg);
		newMsg.print(sendTag);
	}
	
	//��ռͥ�ڵ������ǰ����viewChange
	public void sendTimeOutMsg(Message msg, Queue<Message> msgQue, int rcvrId) {
		Message newMsg = new Message(Message.TimeOut, msg.getCtx(), id, rcvrId, 
				msg.getSeqId()+1, msg.getPriId()+1, 
				msg.getTimestamp() + Settings.timeout/2);
		msgQue.add(newMsg);
		newMsg.print(sendTag);
	}
	
	public int remainCnt(Message msg) {
		String key = msg.getCtx();
		if(!cntMap.containsKey(key)) {
			return 0;
		}
		int cnt = cntMap.get(key);
		cntMap.put(key, cnt-1);
		return cnt;
	}
	
}
