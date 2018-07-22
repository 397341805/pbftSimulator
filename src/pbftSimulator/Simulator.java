package pbftSimulator;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import pbftSimulator.replica.ByztReplica;
import pbftSimulator.replica.Replica;

public class Simulator {
	
	public static Comparator<Message> timeCmp = new Comparator<Message>(){
		@Override
		public int compare(Message c1, Message c2) {
			return (int) (c1.getTimestamp() - c2.getTimestamp());
		}
	};
	
	public static void main(String[] args) {
		//��Ϣ���ȶ��У�����Ϣ�ƻ��������ʱ�������
		Queue<Message> msgQue = new PriorityQueue<>(timeCmp);
		
		//��ʼ������f������ڵ��N��replicas
		int[][] netDlys = netDlyBtwRpInit(Settings.N);
		boolean[] byzts = byztDistriInit(Settings.N, Settings.f);
		Replica[] reps = new Replica[Settings.N];
		for(int i = 0; i < Settings.N; ++i) {
			if(byzts[i]) {
				reps[i] = new ByztReplica(i, byzts[i], netDlys[i]);
			}else {
				reps[i] = new Replica(i, byzts[i], netDlys[i]);
			}
		}
		
		//�ͻ��˷���request��Ϣ
		Message[] reqMsg = reqMsgInit(Settings.reqNum, Settings.avrReqInvl);
		for(Message msg : reqMsg) {
			msgQue.add(msg);
		}
		
		//�ڵ㴦����Ϣ
		while(!msgQue.isEmpty()) {
			Message msg = msgQue.poll();
			reps[msg.getRcvId()].msgProcess(msg, msgQue);
			if(Settings.getNetDelay(msgQue, 0) > Settings.collapseDelay) {
				System.out.println("��Error�����紫����Ϣ����Ϊ"+msgQue.size()+",ϵͳ����ʱ���ѳ���"+Settings.collapseDelay/1000+"�룬�ѱ�����");
				break;
			}
		}
		System.out.println("��The end��");
	}
	
	/*
	 * �����ʼ��replicas�ڵ�֮������紫���ӳ�
	 * int n ��ʾ�ڵ�����
	 */
	public static int[][] netDlyBtwRpInit(int n){
		int[][] ltcs = new int[n][n];
		Random rand = new Random();
		for(int i = 0; i < n; ++i) 
			for(int j = 0; j < n; ++j) 
				if(i < j && ltcs[i][j] == 0) {
					ltcs[i][j] = Settings.baseDlyBtwRp + rand.nextInt(Settings.dlyRngBtwRp);
					ltcs[j][i] = ltcs[i][j];
				}
		return ltcs;
	}
	
	/*
	 * �����ʼ��replicas�ڵ���ͻ��˵����紫���ӳ�
	 * int n ��ʾ�ڵ�����
	 */
	public static int[] netDlyBtwRpAndCliInit(int n){
		int[] ltcs = new int[n];
		Random rand = new Random();
		for(int i = 0; i < n; ++i) 
			ltcs[i] = Settings.baseDlyBtwRpAndCli + rand.nextInt(Settings.dlyRngBtwRpAndCli);
		return ltcs;
	}
	
	/*
	 * �����ʼ��replicas�ڵ��еĶ���ڵ�ֲ�������ڵ㷵��true
	 * int n ��ʾ��ʼ���Ľڵ�����
	 * int f ��ʾ����ڵ������
	 */
	public static boolean[] byztDistriInit(int n, int f) {
		boolean[] byzt = new boolean[n];
		Random rand = new Random();
		while(f > 0) {
			int i = rand.nextInt(n);
			if(!byzt[i]) {
				byzt[i] = true;
				--f;
			}
		}
		return byzt;
	}
	
	/*
	 * ������Ϣ��ʼ��,ƽ��
	 * int k��ʾ������Ϣ����
	 * int avrInvl��ʾrequest�����ƽ��ʱ����
	 */
	public static Message[] reqMsgInit(int k, int avrInvl) {
		Message[] msgs = new Message[Settings.reqNum*Settings.N];
		int n = 0;
		Random rand = new Random();
		int timestamp = 0;
		int[] ltcs = netDlyBtwRpAndCliInit(Settings.N);
		for(int i = 0; i < Settings.reqNum; ++i) {
			//��ÿ���ڵ㷢��request����
			for(int j = 0; j < Settings.N; ++j) {
				msgs[n++] = new Message(Message.Request, "message"+i, 
						-1, j, 0, 0, timestamp + ltcs[j]);
			}
			timestamp += rand.nextInt(avrInvl);
		}
		return msgs;
	}
	
}
