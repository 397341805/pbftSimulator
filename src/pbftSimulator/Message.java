package pbftSimulator;

public class Message {
	
	public static final int Request = 0;
	
	public static final int Preprepare = 1;
	
	public static final int Prepare = 2;
	
	public static final int Commit = 3;
	
	public static final int TimeOut = 4;
	
	public static final int ViewChg = 5;
	
	public static final int NewView = 6;
	
	//��Ϣ���� Request, Preprepare, Prepare, Commit, ViewChange, NewView, Timeout
	private int type;				
	
	private String ctx;				//��Ϣ���ݣ�Ψһ������Ϊkey��
	
	private int sndId;				//��Ϣ���Ͷ�id

	private int rcvId;  			//��Ϣ���ն�id
	
	private int seqId;  			//��Ϣ��������
	
	private int priId;  			//leader id
	
	private long timestamp;  		//��Ϣ����ʱ���
	
	public Message(int type, String ctx, int sndId,
			int rcvId, int seqId, int priId, long timestamp) {
		this.type = type;
		this.ctx = ctx;
		this.sndId = sndId;
		this.rcvId = rcvId;
		this.seqId = seqId;
		this.priId = priId;
		this.timestamp = timestamp;
	}
	
	public void print(String tag) {
		if(!Settings.showDetailInfo) return;
		String prefix = "��"+tag+"��";
		String[] typeName = {"Request","Preprepare","Prepare","Commit","TimeOut","ViewChange","NewView"};
		System.out.println(prefix+"��Ϣ����:"+typeName[type]+";��Ϣ����:"
				+ctx+";������id:"+sndId+";������id:"+rcvId+";��Ϣ��������:"+seqId
				+";���ڵ�id:"+priId+";��Ϣ����ʱ���:"+timestamp
				+";remainConfirms:"+Settings.remainConfirms);
	}
	
	public boolean equals(Message msg) {
		if(msg == null)
			return false;
		if(msg.ctx.equals(ctx) && msg.type == type 
				&& msg.priId == priId && msg.seqId == seqId
				&& msg.rcvId == rcvId && msg.sndId == sndId) {
			return true;
		}
		return false;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCtx() {
		return ctx;
	}

	public void setCtx(String ctx) {
		this.ctx = ctx;
	}

	public int getSndId() {
		return sndId;
	}

	public void setSndId(int sndId) {
		this.sndId = sndId;
	}

	public int getRcvId() {
		return rcvId;
	}

	public void setRcvId(int rcvId) {
		this.rcvId = rcvId;
	}

	public int getSeqId() {
		return seqId;
	}

	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}

	public int getPriId() {
		return priId;
	}

	public void setPriId(int priId) {
		this.priId = priId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
