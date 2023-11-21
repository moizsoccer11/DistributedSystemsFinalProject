import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Server {
    //Other functions
    public static void getAllQuestionsForQueues(Queue<String> scienceQueue,Queue<String> mathQueue,Queue<String> englishQueue){
        try {
            //Get All Questions from Database and put in the Queues
            Connection db = DatabaseConnection.getConnection();
            //Science Queue Retrieval:
            // Retrieve all questions from the 'sciencequestions' table
            String selectQuery = "SELECT question FROM sciencequestions";
            PreparedStatement preparedStatement = db.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery(); {
                // Iterate through the result set and add questions to the queue
                while (resultSet.next()) {
                    String question = resultSet.getString("question");
                    scienceQueue.offer(question);
                }
            }

            //Math Queue Retrieval:
            // Retrieve all questions from the 'mathquestions' table
            selectQuery = "SELECT question FROM mathquestions";
            preparedStatement = db.prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery(); {
                // Iterate through the result set and add questions to the queue
                while (resultSet.next()) {
                    String question = resultSet.getString("question");
                    mathQueue.offer(question);
                }
            }

            //English Queue Retrieval:
            // Retrieve all questions from the 'mathquestions' table
            selectQuery = "SELECT question FROM englishquestions";
            preparedStatement = db.prepareStatement(selectQuery);
            resultSet = preparedStatement.executeQuery(); {
                // Iterate through the result set and add questions to the queue
                while (resultSet.next()) {
                    String question = resultSet.getString("question");
                    englishQueue.offer(question);
                }
            }
            //Close Database connection
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
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
            //Get all questions for queues:
            getAllQuestionsForQueues(scienceQueue, mathQueue, englishQueue);
            Connection db;
            PreparedStatement preparedStatement;
            //Begin Pub/Sub 
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
                            db = DatabaseConnection.getConnection();
                            // Insert data into the 'sciencequestion' table
                            String insertQuery = "INSERT INTO sciencequestions (question) VALUES (?)";
                            //Prepare statement
                            try {
                                preparedStatement = db.prepareStatement(insertQuery);
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
                            break;
                        }
                        case "Math":{
                            mathQueue.offer(question);
                             //Open Database connection:
                            db = DatabaseConnection.getConnection();
                            // Insert data into the 'sciencequestion' table
                            String insertQuery = "INSERT INTO mathquestions (question) VALUES (?)";
                            //Prepare statement
                            try {
                                preparedStatement = db.prepareStatement(insertQuery);
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
                            break;
                        }
                        case "English":{
                            englishQueue.offer(question);
                             //Open Database connection:
                            db = DatabaseConnection.getConnection();
                            // Insert data into the 'sciencequestion' table
                            String insertQuery = "INSERT INTO englishquestions (question) VALUES (?)";
                            //Prepare statement
                            try {
                                preparedStatement = db.prepareStatement(insertQuery);
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
                            break;
                        }
                    }
                }
                //Check for any incoming requests for data/questions
                //Incoming workers
                byte[] requestBytes =requestSocket.recv(ZMQ.NOBLOCK);
                if(requestBytes != null){
                    //Save the recieved request in string
                    String request= new String(requestBytes,ZMQ.CHARSET);
                    System.out.println(request);
                    splitter=request.split("/");
                    if(splitter[1].equals("Science")){
                       for(int i=0; i<Integer.parseInt(splitter[0]); i++){
                            //deqeue a task from queue
                            String nextQuestion = scienceQueue.poll();
                            //check if there is a task
                            if(nextQuestion != null){
                                System.out.println("Sending Question:" + nextQuestion);
                                responseSocket.send(nextQuestion.getBytes(),0);
                            }
                        }
                    }
                    if(splitter[1].equals("Math")){
                       for(int i=0; i<Integer.parseInt(splitter[0]); i++){
                            //deqeue a task from queue
                            String nextQuestion = mathQueue.poll();
                            //check if there is a task
                            if(nextQuestion != null){
                                System.out.println("Sending Question:" + nextQuestion);
                                responseSocket.send(nextQuestion.getBytes(),0);
                            }
                        }
                    }
                    if(splitter[1].equals("English")){
                       for(int i=0; i<Integer.parseInt(splitter[0]); i++){
                            //deqeue a task from queue
                            String nextQuestion = englishQueue.poll();
                            //check if there is a task
                            if(nextQuestion != null){
                                System.out.println("Sending Question:" + nextQuestion);
                                responseSocket.send(nextQuestion.getBytes(),0);
                            }
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
