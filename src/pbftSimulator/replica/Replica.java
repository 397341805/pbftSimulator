package pbftSimulator.replica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import pbftSimulator.Message;
import pbftSimulator.Settings;
import pbftSimulator.Status;
import pbftSimulator.Utils;

public class Replica {
	
	protected String receiveTag;
	
	protected String sendTag;
	
	protected String processTag;
	
	protected boolean isByzt;					// �Ƿ��ǰ�ռͥ�ڵ�
	
	protected int id; 								//��ǰ�ڵ��id
	
	protected HashMap<String, Status> statMap;	//��Ϣ״̬map
	
	protected HashMap<String, ArrayList<Message>> msgCache;  //δ������Ϣ����
	
	protected int netDlys[];				//�������ڵ�������ӳ�
	
	public Replica(int id, boolean isByzt, int[] netDlys) {
		this.id = id;
		this.isByzt = isByzt;
		statMap = new HashMap<>();
		msgCache = new HashMap<>();
		receiveTag = Settings.receiveTag;
		sendTag = Settings.sendTag;
		processTag = Settings.processTag;
		this.netDlys = netDlys;
	}
	
	public void msgProcess(Message msg, Queue<Message> msgQue) {
		msg.print(receiveTag);
		int type = msg.getType();
		switch(type) {
		case Message.Request:
			receiveRequest(msg, msgQue, msg.getTimestamp());
			break;
		case Message.Preprepare:
			receivePreprepare(msg, msgQue, msg.getTimestamp());
			break;
		case Message.Prepare:
			receivePrepare(msg, msgQue, msg.getTimestamp());
			break;
		case Message.Commit:
			receiveCommit(msg, msgQue);
			break;
		case Message.ViewChg:
			receiveViewChange(msg, msgQue, msg.getTimestamp());
			break;
		case Message.NewView:
			receiveNewView(msg, msgQue, msg.getTimestamp());
			break;
		case Message.TimeOut:
			receiveTimeOut(msg, msgQue);
			break;
		default:
			System.out.println("��Error����Ϣ���ʹ���");
		}
	}
	
	public void receiveRequest(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		//����һ��Timeout��Ϣ���Ա㽫������Ƿ�����ʱ
		sendTimeOutMsg(msg, msgQue, id);
		requestProcess(msg, msgQue, procTime);
		//������Ϣ�����п��ܴ��ڵĺ�����Ϣ
		postMsgProcess(msg, msgQue, Message.Preprepare, procTime);
	}
	
	public void receivePreprepare(Message msg, Queue<Message> msgQue, long procTime) {
		//�����Ϣ�Ƿ�Ϲ棬��������Ӧ�Ĵ���
		if(!preCheckMsg(msg)) return;
		preprepareProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.Prepare, procTime);
	}
	
	public void receivePrepare(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		prepareProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.Commit, procTime);
	}
	
	public void receiveCommit(Message msg, Queue<Message> msgQue) {
		if(!preCheckMsg(msg)) return;
		commitProcess(msg, msgQue);
	}
	
	public void receiveTimeOut(Message msg, Queue<Message> msgQue) {
		if(!preCheckMsg(msg)) return;
		timeOutProcess(msg, msgQue);
	}
	
	
	public void receiveViewChange(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		viewChangeProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.NewView, procTime);
	}
	
	public void receiveNewView(Message msg, Queue<Message> msgQue, long procTime) {
		if(!preCheckMsg(msg)) return;
		newViewProcess(msg, msgQue, procTime);
		postMsgProcess(msg, msgQue, Message.Preprepare, procTime);
	}
	
	public void requestProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		//�½�״̬
		statMap.put(msg.getCtx(), new Status(Status.Request, msg.getSeqId(), msg.getPriId(), msg.getTimestamp()));
		//��������ڵ㣬�ͷ���preprepare��Ϣ��prepare��Ϣ��������״̬
		if(id == msg.getPriId()) {
			broadcastMsg(Message.Preprepare, msg, msgQue, procTime);	
		}
	}
	
	public void preprepareProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		stat.setStage(Status.Preprepare);
		broadcastMsg(Message.Prepare, msg, msgQue, procTime);
	}
	
	public void prepareProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addPrepareVote(msg.getSndId());
		if(Utils.reachMajority(voteNum)) {
			stat.setStage(Status.Prepare);
			broadcastMsg(Message.Commit, msg, msgQue, procTime);
		}
	}
	
	public void commitProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addCommitVote(msg.getSndId());
		if(Utils.reachMajority(voteNum)) {
			stat.setStage(Status.Commit);
			stat.setEndTime(msg.getTimestamp());
			//����Ѿ������ȶ�״̬����Ϣ����
			msgCache.remove(msg.getCtx());
			//��ӡ�����Ϣ
			System.out.println("��Confirmed����Ϣ:"
					+msg.getCtx()+"�ڽڵ�"+id+"("+(isByzt?"����ڵ�":"�����ڵ�")+")������̬����Ϣȷ��ʱ��:"
					+(stat.getEndTime()-stat.getStartTime())
					+";��Ϣ����:"+stat.getSeqId()
					+";���ڵ�:"+stat.getPriId()
					);
			//����ȫ�ֿ��Ʊ�����״̬
			Settings.remainConfirms--;
		}
	}
	
	public void timeOutProcess(Message msg, Queue<Message> msgQue) {
		msg.print(processTag);
		broadcastMsg(Message.ViewChg, msg, msgQue, msg.getTimestamp());
	}
	
	public void viewChangeProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addViewChgVote(msg);
		if(Utils.reachMajority(voteNum)) {
			int rvcId = msg.getPriId();
			sendOneMsg(Message.NewView, msg, msgQue, rvcId, procTime);
			//����״̬ΪRequest������primaryId��SequenceId
			stat.reset(msg.getSeqId(), msg.getPriId());
			//����һ��Timeout��Ϣ���Ա㽫������Ƿ�����ʱ
			sendTimeOutMsg(msg, msgQue, id);
		}
	}
	
	public void newViewProcess(Message msg, Queue<Message> msgQue, long procTime) {
		msg.print(processTag);
		Status stat = statMap.get(msg.getCtx());
		int voteNum = stat.addNewViewVote(msg.getSndId());
		if(Utils.reachMajority(voteNum)) {
			//����״̬
			stat.reset(msg.getSeqId(), msg.getPriId());
			broadcastMsg(Message.Preprepare, msg, msgQue, procTime);
		}
	}
	
	public boolean preCheckMsg(Message msg) {
		String key = msg.getCtx();
		int type = msg.getType();
		//�����Ϣ��request��Ϣ,�����request�����Ѿ����������ô���Թ�������Ϣ������
		if(type == Message.Request) {
			if(statMap.containsKey(key)) return false;
			else return true;
		}
		//�������request��Ϣ������û��������Ϣ��״̬����ô����Ϣ�浽�������Ժ���
		if(!statMap.containsKey(key)) {
			addMsgToCache(msg);
			return false;
		}  
		Status stat = statMap.get(key);
		//�����Ϣ��sequenceId���ڵ�ǰ��Ϣ״̬��sequenceId���Թ�������Ϣ������
		if(msg.getSeqId() < stat.getSeqId()) return false;
		//�����Ϣ������ViewChg����NewView����Timeout
		//����Ϣ�Ѿ�������̬�����Թ�������Ϣ������
		if(type == Message.ViewChg || type == Message.NewView || type == Message.TimeOut) {
			if(stat.getStage() == Status.Commit) return false;
			return true;
		}
		//��Ϣ������Preprepare����Prepare����Commit
		//�����Ϣ�����ڵ�id����Ϣ״̬�����ڵ�id��ͬ����seqId���ڵ�ǰ����Ϣ״̬��seqId
		//������Ϣ����Ϣ�����е�ȷ���µ���ͼ����������
		if(msg.getPriId() != stat.getPriId() || msg.getSeqId() > stat.getSeqId()) {
			addMsgToCache(msg);
			return false;
		}
		//�����Ϣ�����͵��ڵ�ǰ��״̬�����Բ�������
		if(msg.getType() < stat.getStage()) return false;
		//�����Ϣ�����͸��ڵ�ǰ��״̬��������Ϣ������������
		if(msg.getType() > stat.getStage()) {
			addMsgToCache(msg);
			return false;
		}
		return true;
	}
	
	public void postMsgProcess(Message msg, Queue<Message> msgQue, int msgType, long procTime) {
		ArrayList<Message> nextMsg = getMsgByType(msgType, msg);
		for(Message m : nextMsg) {
			switch(msgType) {
			case Message.Preprepare:
				receivePreprepare(m, msgQue, procTime);
				break;
			case Message.Prepare:
				receivePrepare(m, msgQue, procTime);
				break;
			case Message.Commit:
				receiveCommit(m, msgQue);
				break;
			case Message.NewView:
				receiveNewView(m, msgQue, procTime);
				break;
			default:
				break;
			}
			
		}
	}
	
	public void broadcastMsg(int type, Message msg, Queue<Message> msgQue, long procTime) {
		for(int i = 0; i < Settings.N; ++i) {
			Message newMsg = new Message(type, msg.getCtx(), id, i, 
					msg.getSeqId(), msg.getPriId(), 
					procTime + Settings.getNetDelay(msgQue, netDlys[i]));
			msgQue.add(newMsg);
			newMsg.print(sendTag);
		}
	}
	
	public void sendOneMsg(int type, Message msg, Queue<Message> msgQue, int rcvId, long procTime) {
		Message newMsg = new Message(type, msg.getCtx(), id, rcvId, 
				msg.getSeqId(), msg.getPriId(), 
				procTime + Settings.getNetDelay(msgQue, netDlys[rcvId]));
		msgQue.add(newMsg);
		newMsg.print(sendTag);
	}
	
	public void sendTimeOutMsg(Message msg, Queue<Message> msgQue, int rcvrId) {
		Message newMsg = new Message(Message.TimeOut, msg.getCtx(), id, rcvrId, 
				msg.getSeqId()+1, msg.getPriId()+1, 
				msg.getTimestamp() + Settings.timeout);
		msgQue.add(newMsg);
		newMsg.print("send");
	}
	
	/*
	 * ����Ϣ�����л�ȡָ����Ϣ���ݵ�ָ�����͵���Ϣ
	 * ��Ҫע����ǣ�����viewChange����Ҫ��֮ǰ����ͬ���͵���Ϣ��cache��ɾȥ
	 */
	public ArrayList<Message> getMsgByType(int type, Message msg) {
		String key = msg.getCtx();
		ArrayList<Message> rstList = new ArrayList<>();
		if(!msgCache.containsKey(key)) {
			return rstList;
		}
		ArrayList<Message> msgList = msgCache.get(key);
		for(Message m: msgList) {
			//��Ҫ��Ϣ���ݣ�primaryId��sequentId��һ�²ŷ��أ�
			//��Ϣ���ݺ���Ϣ��������id���ᱻ�۸ģ���ժҪ��ǩ����
			if(type == m.getType() && msg.getPriId() == m.getPriId() 
					&& msg.getSeqId() == m.getSeqId()) {
				rstList.add(m);
			}
		}
		return rstList;
	}
	
	/*
	 * ����Ϣ������������Ϣ
	 */
	public void addMsgToCache(Message msg) {
		String key = msg.getCtx();
		if(!msgCache.containsKey(key)) {
			ArrayList<Message> list = new ArrayList<>();
			list.add(msg);
			msgCache.put(key, list);
			return;
		}
		//ע�⣺������ظ����͵���Ϣ���Ͳ���Ҫ����
		ArrayList<Message> msgList = msgCache.get(key);
		boolean flag = true;
		for(Message m : msgList) {
			if(m.equals(msg)) {
				flag = false;
			}
		}
		if(flag) {
			msgList.add(msg);
		}
	}
	
}
