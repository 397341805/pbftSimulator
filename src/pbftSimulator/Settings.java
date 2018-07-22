package pbftSimulator;

import java.util.Queue;

public class Settings {

	public static final int N = 20;  						//replicas�ڵ������
	
	public static final int f = 6;							//����ڵ������
	
	public static final int reqNum = 10;						//�ͻ��˵�������Ϣ����
	
	public static final int avrReqInvl = 1000; 				//������Ϣ��ƽ��ʱ����(����)
	
	public static final int timeout = 500;					//��ʱ�趨(����)
	
	public static final int baseDlyBtwRp = 10;				//�ڵ�֮��Ļ�������ʱ��
	
	public static final int dlyRngBtwRp = 50;				//�ڵ�������ʱ���Ŷ���Χ
	
	public static final int baseDlyBtwRpAndCli = 50;		//�ڵ���ͻ���֮��Ļ�������ʱ��
	
	public static final int dlyRngBtwRpAndCli = 150;		//�ڵ���ͻ���֮�������ʱ���Ŷ���Χ
	
	public static final int bandwidth = 10000;				//�ڵ������Ķ��Ϣ����(������ʱ�ӳ�ָ��������)
	
	public static final double factor = 1.05;					//��������غ��ָ������
	
	public static final int collapseDelay = 10000;				//��Ϊϵͳ����������ʱ��
	
	public static final boolean showDetailInfo = false;		//�Ƿ���ʾ��������Ϣ��������
	
	public static final String receiveTag = "Receive";
	
	public static final String sendTag = "Send";
	
	public static final String offlineTag = "Receive";
	
	public static final String processTag = "Process";
	
	public static final String disconnectTag = "Disconnect";
	
	public static final String confirmedTag = "Confirmed";
	
	public static final String btztProcessTag = "BtztProcess";
	
	public static final String btztReceiveTag = "BtztReceive";
	
	public static final String btztSendTag = "BtztSend";
	
	public static int remainConfirms = reqNum * (N - f);
	
	public static int getNetDelay(Queue<Message> msgQue, int basedelay) {
		int msgNum = msgQue.size();
		if(msgNum < bandwidth) {
			return basedelay;
		}else {
			return (int)Math.pow(factor, msgNum - bandwidth) + basedelay;
		}
	}
	
}
