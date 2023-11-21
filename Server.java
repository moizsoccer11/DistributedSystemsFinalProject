import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Server {
    
    //Main Function
    public static void main(String[] args) {
        //Queues:
        //Science Question Queues
        Queue<String> scienceQueue = new LinkedList<>();
        //Math Question Queues
        Queue<String> mathQueue = new LinkedList<>();
        //English Question Queue
        Queue<String> englishQueue = new LinkedList<>();

        try {

            //Create Z Context
            ZContext context = new ZContext(1);

            //Create Sockets
            ZMQ.Socket queue = context.createSocket(SocketType.PULL); //Data recieved from Publisher
            ZMQ.Socket requestSocket = context.createSocket(SocketType.PULL); //Data requested by Subscriber
            ZMQ.Socket responseSocket = context.createSocket(SocketType.PUSH); //Data sent to Subscriber
            
            //bindings
            queue.bind("tcp://*:5555");
            requestSocket.bind("tcp://*:5556");
            responseSocket.bind("tcp://*:5557");

            //
            System.out.println("Server Started....");
            //
            while(true){
                String[] splitter = new String[2];
                //Check if any data has been sent by publisher
                byte [] questionBytes = queue.recv(ZMQ.NOBLOCK);
                //Check if any tasks avaialble
                if(questionBytes != null){
                    String question = new String(questionBytes,StandardCharsets.UTF_8);
                    //Determine which queue this question fall into and add to respective queue as well as in SQL database
                    splitter= question.split("/");
                    question= splitter[0];
                    System.out.println("Recieved Question:" + question.toString());
                    switch (splitter[1]) {
                        case "Science":{
                            scienceQueue.offer(question);
                            //Open Database connection:
                            Connection db = DatabaseConnection.getConnection();
                            // Insert data into the 'sciencequestion' table
                            String insertQuery = "INSERT INTO sciencequestions (question) VALUES (?)";
                            //Prepare statement
                            try {
                                PreparedStatement preparedStatement = db.prepareStatement(insertQuery);
                                preparedStatement.setString(1, question);
                                //execute query
                                int rowsAffected = preparedStatement.executeUpdate();
                                //check if query executed correctly or not
                                if (rowsAffected > 0) {
                                    System.out.println("Question inserted into Database!");
                                } else {
                                    System.out.println("Failed to insert data.");
                                }
                                db.close();
                                
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        case "Math":{
                            mathQueue.offer(question);
                             //Open Database connection:
                            Connection db = DatabaseConnection.getConnection();
                            // Insert data into the 'sciencequestion' table
                            String insertQuery = "INSERT INTO mathquestions (question) VALUES (?)";
                            //Prepare statement
                            try {
                                PreparedStatement preparedStatement = db.prepareStatement(insertQuery);
                                preparedStatement.setString(1, question);
                                //execute query
                                int rowsAffected = preparedStatement.executeUpdate();
                                //check if query executed correctly or not
                                if (rowsAffected > 0) {
                                    System.out.println("Question inserted into Database!");
                                } else {
                                    System.out.println("Failed to insert data.");
                                }
                                db.close();
                                
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        case "English":{
                            englishQueue.offer(question);
                             //Open Database connection:
                            Connection db = DatabaseConnection.getConnection();
                            // Insert data into the 'sciencequestion' table
                            String insertQuery = "INSERT INTO englishquestions (question) VALUES (?)";
                            //Prepare statement
                            try {
                                PreparedStatement preparedStatement = db.prepareStatement(insertQuery);
                                preparedStatement.setString(1, question);
                                //execute query
                                int rowsAffected = preparedStatement.executeUpdate();
                                //check if query executed correctly or not
                                if (rowsAffected > 0) {
                                    System.out.println("Question inserted into Database!");
                                } else {
                                    System.out.println("Failed to insert data.");
                                }
                                db.close();
                                
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                //Check for any incoming requests for data/questions

            }


            











        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
