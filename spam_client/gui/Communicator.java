package filter.bayesian.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import filter.bayesian.common.Message;
import filter.bayesian.common.Utility;
import filter.bayesian.gui.MyForm;
import java.net.SocketException;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class Communicator {

    // the output stream on the socket
    private DataOutputStream out = null;
    // the input stream on the socket
    private DataInputStream dataInputStream = null;
    // the socket bound to a specific client.
    private Socket soc = null;
    private MyForm myForm = null;
    

    /**
     * @param args
     */
    public static void main(String[] args) {
    }
    private double misClassifiedAsSpamCount;
    private double totalSpamCount;
    private double misClassifiedAsHamCount;
    private double totalHamCount;
    private double actualSpamCount;
    private double actualHamCount;
    private int inboxMailCount;

    public Communicator(MyForm myForm) {
        this.myForm = myForm;
        initialize();
    }

    private void initialize() {

        try {
            soc = new Socket("sand.cise.ufl.edu", 3132);
//            soc = new Socket("localhost", 3132);
            out = new DataOutputStream(soc.getOutputStream());
            dataInputStream = new DataInputStream(soc.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String issueCommand(byte command, String data) {
        try {
            System.out.println("Issuing command");

            switch (command) {
                case Message.DATA_CLASSIFY_MESSAGES:
                    this.actualHamCount = 0;
                    this.actualSpamCount = 0;
                    this.misClassifiedAsHamCount = 0;
                    this.misClassifiedAsSpamCount = 0;
                    this.totalHamCount = 0;
                    this.totalSpamCount = 0;
                    
                     ((DefaultTableModel) myForm.getSpamClassificationTable().getModel()).setRowCount(0);
                      ((DefaultTableModel) myForm.getHamClassificationTable().getModel()).setRowCount(0);
                    byte[] msg = Message.getDataMessage(data, Message.DATA_CLASSIFY_MESSAGES);
                    yellToClient(msg);
                    Message incoming;
                    //Change the logic here. First accept the total number of
                    //email messages to be classified then run the for loop
                    for(int index = 0; index<inboxMailCount; ++index){
                        incoming = waitAndProcessIncomingRequest(dataInputStream);
                        handleClassifyMessage(incoming);

                    }
                    myForm.getTotalSpamCount().setText(Double.toString(totalSpamCount));
                    myForm.getTotalHamCount().setText(Double.toString(totalHamCount));
                    myForm.getMisClassifiedSpamCount().setText(Double.toString(misClassifiedAsSpamCount));
                    myForm.getMisClassifiedHamCount().setText(Double.toString(misClassifiedAsHamCount));
                    myForm.getActualHamCount().setText(Double.toString(actualHamCount));
                    myForm.getActualSpamCount().setText(Double.toString(actualSpamCount));

                    double accuracy = ((actualSpamCount - misClassifiedAsHamCount)/(actualSpamCount - misClassifiedAsHamCount + misClassifiedAsSpamCount))*100;
                    myForm.getAccuracyCount().setText(Double.toString(accuracy));

                    System.out.println("**************Experimental Results****************");
                    double LtoL = actualHamCount - misClassifiedAsSpamCount;
                    double StoS = actualSpamCount - misClassifiedAsHamCount;
                    double newAccuracy = (LtoL + StoS)/(actualHamCount + actualSpamCount);
                    double LtoS = misClassifiedAsSpamCount;
                    double StoL = misClassifiedAsHamCount;

                    double weightedAccuracy = ((9 * LtoL) + StoS)/((9 * actualHamCount) + actualSpamCount);
                    
                    
                    double TCR = actualSpamCount/((9*LtoS)+StoL);
                    myForm.getAccuracyLabel().setText("" + weightedAccuracy * 100);
                    myForm.getThresholdLabel().setText(".9");
                    myForm.getTrcLabel().setText(""+ TCR);

                    System.out.println("LtoL "+LtoL);
                    System.out.println("StoS "+StoS);
                    System.out.println("LtoS "+LtoS);
                    System.out.println("StoL "+StoL);
                    System.out.println("misClassifiedAsSpamCount: " +misClassifiedAsSpamCount);
                    System.out.println("misClassifiedAsHamCount: " +misClassifiedAsHamCount);
                    System.out.println("StoL "+StoL);

                    System.out.println("Actual Spam Count "+actualSpamCount);
                    System.out.println("Actual Ham Count "+actualHamCount);
                    System.out.println("Cost Sensitive Accuracy: "+newAccuracy);
                    System.out.println("Cost Sensitive Weighted Accuracy: "+weightedAccuracy);
                    System.out.println("TCR :"+TCR);
                    break;


                case Message.DATA_LOAD_INBOX:
                    ((DefaultTableModel) myForm.getInboxTable().getModel()).setRowCount(0);
                    msg = Message.getDataMessage(data, Message.DATA_LOAD_INBOX);
                    yellToClient(msg);
                    incoming = waitAndProcessIncomingRequest(dataInputStream);
                    System.out.println(incoming.getMessage());

                    if(incoming.getType() == Message.DATA_INBOX){
                        inboxMailCount = Integer.parseInt(incoming.getMessage());
                        for(int index = 0; index<inboxMailCount;++index)
                        {

                            myForm.getInboxMessageCount().setText(Integer.toString(index+1));

                            incoming = waitAndProcessIncomingRequest(dataInputStream);
                            handleLoadInboxMessage(incoming);
                        }


                    }

                    break;

                case Message.DATA_REPORT_SPAM:
                    msg = Message.getDataMessage(data, command);
                    yellToClient(msg);
                    break;
                case Message.REQUEST_FEATURE_DETAILS:
                    //byte[] msg = Message.getDataMessage(data, );
                    yellToClient(Message.REQUEST_FEATURE_DETAILS);
                    System.out.println("I sent the message !");
                    incoming = waitAndProcessIncomingRequest(dataInputStream);
                    handleFeatureDetailsMessage(incoming);
                    break;
                case Message.DATA_DIRECTORIES:
                    msg = Message.getDataMessage(data, command);
                    yellToClient(msg);
                    incoming = waitAndProcessIncomingRequest(dataInputStream);
                    if (incoming.getType() == Message.DATA_START_UP_STATS) {
                        return incoming.getMessage();
                    }
                    break;
                case Message.DATA_TO_TRAIN:
                    msg = Message.getDataMessage(data, Message.DATA_TO_TRAIN);
                    yellToClient(msg);


                    int numberOfSpams = myForm.getNumberOfSpams();
                    int numberOfHams = myForm.getNumberOfHams();


                    while (true) {
                        incoming = waitAndProcessIncomingRequest(dataInputStream);


                        if (incoming.getType() == Message.DATA_PROCESSED_SPAM_COUNTER) {
                            MySwingWorker spamWorker = myForm.getSpamWorker();
                            System.out.println(incoming.getMessage());
                            int progress = Integer.parseInt(incoming.getMessage());
                            System.out.println(progress);
                            myForm.getNumSpamProcessed().setText(incoming.getMessage());
                            int percProgress = (progress * 100) / numberOfSpams;
                            System.out.println("Percent : " + percProgress);
                            spamWorker.setProgressBar(percProgress);
                            if (percProgress == 100) {
                            	myForm.getStartTrainingButton().setSize(100, 300);
                                myForm.getStartTrainingButton().setText("Fetching Feats.");
                            }

                        } else if (incoming.getType() == Message.DATA_PROCESSED_HAM_COUNTER) {
                            MySwingWorker hamWorker = myForm.getHamWorker();
                            System.out.println(incoming.getMessage());
                            int progress = Integer.parseInt(incoming.getMessage());
                            System.out.println(progress);
                            myForm.getNumHamProcessed().setText(incoming.getMessage());
                            int percProgress = (progress * 100) / numberOfHams;
                            System.out.println("Percent : " + percProgress);
                            hamWorker.setProgressBar(percProgress);
                            if (percProgress == 100) {
                            	myForm.getStartTrainingButton().setSize(100, 300);
                                // show processing dialog
                                myForm.getStartTrainingButton().setText("Fetching Feats.");
                            }
                        } else if (incoming.getType() == Message.DATA_FEATURE_SIZE) {
                            myForm.getTotalFeatures().setText(incoming.getMessage());
                        } else if (incoming.getType() == Message.DATA_FEATURE_DETAILS) {
                            ((DefaultTableModel) myForm.getFeatureTable().getModel()).setRowCount(0);
                            handleFeatureDetailsMessage(incoming);
                            // remove processing dialog
                            break;
                        }
                    }
                    System.out.println("Just exited while loop...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void handleFeatureDetailsMessage(Message incoming) {
        myForm.getNextButton().setVisible(true);
        System.out.println("Received features: " + incoming.getMessage());
        String[] records = incoming.getMessage().split("\n");
        JTable featureTable = myForm.getFeatureTable();
//                            TableModel model = featureTable.getModel();
        int rowCount = featureTable.getModel().getRowCount();
        System.out.println(rowCount);
        System.out.println(records.length);

        ((DefaultTableModel) featureTable.getModel()).setRowCount(rowCount + 100);

        for (int index = rowCount; index < (rowCount + records.length); ++index) {

            String[] featureDetails = records[index - rowCount].split("%");
            System.out.println(featureDetails[0] + "," + featureDetails[1] + "," +
                    featureDetails[2] + "," + featureDetails[3]);
            featureTable.setValueAt(index + 1, index, 0);
            featureTable.setValueAt(featureDetails[0], index, 1);
            featureTable.setValueAt(featureDetails[1], index, 2);
            featureTable.setValueAt(featureDetails[2], index, 3);
            featureTable.setValueAt(featureDetails[3], index, 4);
        }

    }

     private void handleLoadInboxMessage(Message incoming){
        String message = incoming.getMessage();

        String messageDetails[] = message.split(";");

        JTable inboxTable = myForm.getInboxTable();

        int rowCount = inboxTable.getModel().getRowCount();

        ((DefaultTableModel) inboxTable.getModel()).setRowCount(rowCount + 1);

        inboxTable.setValueAt(messageDetails[1], rowCount, 0);
        inboxTable.setValueAt(messageDetails[2], rowCount, 1);
        inboxTable.setValueAt(messageDetails[3], rowCount, 2);
        inboxTable.setValueAt(messageDetails[0], rowCount, 3);
//        inboxTable.setValueAt(fileName, rowCount, 1);
//        inboxTable.setValueAt(fileName, rowCount, 2);
//        inboxTable.setValueAt(fileName, rowCount, 3);



    }

    private void handleClassifyMessage(Message incoming){
        String fileClassification = incoming.getMessage();

        String classification [] = fileClassification.split(";");

        JTable spamClassificationTable = myForm.getSpamClassificationTable();
        JTable hamClassifiactionTable = myForm.getHamClassificationTable();

        int spamRowCount = spamClassificationTable.getModel().getRowCount();
        int hamRowCount = hamClassifiactionTable.getModel().getRowCount();

        if(classification[1].compareTo("spam")==0){
            if(classification[0].contains("ham")){
                misClassifiedAsSpamCount++;
            }


            ((DefaultTableModel) spamClassificationTable.getModel()).setRowCount(spamRowCount + 1);
            spamClassificationTable.setValueAt(classification[0],spamRowCount,0);
            spamClassificationTable.setValueAt(classification[1], spamRowCount, 1);
            totalSpamCount++;
        }else{
            if(classification[0].contains("spam")){
                misClassifiedAsHamCount++;
            }
            ((DefaultTableModel) hamClassifiactionTable.getModel()).setRowCount(hamRowCount + 1);
            hamClassifiactionTable.setValueAt(classification[0], hamRowCount, 0);
            hamClassifiactionTable.setValueAt(classification[1], hamRowCount, 1);
            totalHamCount++;
        }
        if(classification[0].contains("spam")){
            actualSpamCount++;
        }else{
            actualHamCount++;
        }
   }


    private void run() {

        try {
            String path = "C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\train_spam" + ";C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\train_ham";

            String path2 = path + ";C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\test_spam" + ";C:\\Documents and Settings\\Kunal Mehrotra\\My Documents\\Downloads\\trec06p.tgz\\trec06p\\trec06p\\test_ham";

            byte[] msg = Message.getDataMessage(path2, Message.DATA_DIRECTORIES);

            yellToClient(msg);
            Message incoming = waitAndProcessIncomingRequest(dataInputStream);

            if (incoming.getType() == Message.DATA_START_UP_STATS) {
                System.out.println(incoming.getMessage());
            }

        // String path = "/adobra/amit2/spam;/adobra/amit2/ham";

//			byte[] msg = Message.getDataMessage(path, Message.DATA_TO_TRAIN);
//			yellToClient(msg);
//
//			Message incoming;

//			while (true) {
//				incoming = waitAndProcessIncomingRequest(dataInputStream);
//				if (incoming.getType() == Message.DATA_PROCESSED_HAM_COUNTER) {
//					System.out.println("HAM::::" + incoming.getMessage());
//				} else if (incoming.getType() == Message.DATA_PROCESSED_SPAM_COUNTER) {
//					System.out.println("SPAM::::" + incoming.getMessage());
//				} else if (incoming.getType() == Message.DATA_FEATURE_DETAILS) {
//
//					System.out.println("Printing ..100 records..");
//
//					String[] featureRecords = incoming.getMessage().split("\n");
//					for (int index = 0; index < featureRecords.length; ++index) {
//						String str = featureRecords[index];
//						String[] featureDetails = str.split("%");
//						String token = featureDetails[0];
//						String spamCount = featureDetails[1];
//						String hamCount = featureDetails[2];
//						String featureProb = featureDetails[3];
//
//						System.out.println(token + ":" + ":" + spamCount + ":"
//								+ hamCount + ":" + featureProb);
//
//					}
//
//				} else if (incoming.getType() == Message.REQUEST_FEATURE_DETAILS) {
//					yellToClient(Message.REQUEST_FEATURE_DETAILS);
//					Thread.currentThread().sleep(6000);
//					yellToClient(Message.REQUEST_FEATURE_DETAILS);
//
//				}
//			}

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Waits for the incoming request, processes it and returns a parsed
     * message.
     *
     * @return Message The parsed message
     *
     * @throws IOException
     *             Exception while waiting/reading from input stream.
     */
    private Message waitAndProcessIncomingRequest(
            DataInputStream dataInputStream) throws IOException {

        Message message = new Message();

        // read the type of the message
        byte type[] = new byte[1];
        dataInputStream.readFully(type);

        if (Message.isData(type[0])) {
            // read the length of the message.
            byte[] length = new byte[4];
            dataInputStream.readFully(length);
            int len = Utility.byteArrayToInt(length);

            // read the payload
            byte[] payload = new byte[len];
            dataInputStream.readFully(payload);
            String msg = new String(payload);

            // encapsulate the data in Message
            message.setType(type[0]);
            message.setLength(len);
            message.setMessage(msg);
        } else {
            message.setType(type[0]);
        }

        return message;
    }

    /**
     * Outputs data to the client.
     *
     * @param message[]
     *            The message to output.
     * @throws IOException
     */
    public synchronized void yellToClient(byte[] message) throws IOException {
        out.write(message);
        out.flush();
    }

    /**
     * Outputs data to the client.
     *
     * @param message
     *            The message to output.
     * @throws IOException
     */
    public synchronized void yellToClient(byte message) throws IOException {
        out.write(message);
        out.flush();
    }
}
