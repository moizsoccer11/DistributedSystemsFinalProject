import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.nio.charset.Charset;
import java.util.Scanner;
import org.zeromq.ZMQ;

import org.zeromq.ZContext;
import org.zeromq.SocketType;
//A teacher(client) can act as a publisher and a worker, depending on if they are adding to queue or requesting from queue
public class Client{
    //Function to Check If user login exists
    public static String[] GetLoginDetails(String loginID){
        String[] loginDetails = {};
        try {
        File myObj = new File("login.txt");
        Scanner myReader = new Scanner(myObj);
        while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.contains(loginID)){
                  loginDetails= data.split("/");
                }
        }
        myReader.close();
        } catch (Exception e) {
           
        }
        return loginDetails;
    }
    //Function to Create new User Login ID
    public static void CreateLoginID(String userName, String loginID){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("login.txt",true));
                writer.write(loginID+"/"+userName);
                writer.newLine();
                writer.close();
            } catch (IOException e) {
        }
    }
    
    public static void main(String[] args) {
        //Variables
        BufferedReader stdIn;
        boolean loggedIn =false;
        String loginID;
        String userName="";
        String[] loginDetails={};
        String userInput;
        try {
            //Create Z Context
            ZMQ.Context context = ZMQ.context(1);
            //Create Buffered Reader
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            //Login User Sequence
            System.out.println("Welcome to Quiz System! If you have a Login ID please Enter, If you don't type '1'");
            userInput= stdIn.readLine();
            if(userInput.equals("1")){
                System.out.println("Please enter a Login ID:");
                userInput = stdIn.readLine();
                loginID=userInput;
                System.out.println("Please enter your user name:");
                userInput = stdIn.readLine();
                userName=userInput;
                CreateLoginID(loginID,userName);
                loggedIn=true;
            }
            else{
                loginDetails = GetLoginDetails(userInput);
                if(loginDetails.length > 1){
                    loggedIn=true;
                    userName=loginDetails[1];
                }
            }
            while(!loggedIn){
                System.out.println("Incorrect Login ID, If you don't have one type '1'");
                userInput= stdIn.readLine();
                if(userInput.equals("1")){
                    System.out.println("Please enter a Login ID:");
                    userInput = stdIn.readLine();
                    loginID=userInput;
                    System.out.println("Please enter your user name:");
                    userInput = stdIn.readLine();
                    userName=userInput;
                    CreateLoginID(loginID,userName);
                    loggedIn=true;
                }
                else{
                    loginDetails = GetLoginDetails(userInput);
                    if(loginDetails.length > 1){
                        loggedIn=true;
                        userName=loginDetails[1];
                    }
                }
            }
            //Welcome the client
            System.out.println("Welcome "+userName+"!\n");
            //Once Logged in determine If Client wants to add questions to Quiz System or Take Questions from Queue
            while(true){
                System.out.println("Select the following services:");
                System.out.println("-------------------------\n");
                System.out.println("1 - Add a quiz question");
                System.out.println("2 - Get quiz question(s)");
                System.out.println("3 - Generate a random test");
                System.out.println("4 - View my submitted questions");
                System.out.println("5 - Quit\n");

                //get user input
                userInput = stdIn.readLine();
                ///Action based on userinput:
                switch(userInput){
                    //Add Quiz Question
                    case "1":{
                        //Variable to hold question
                        String question="";
                        //Variable to hold subject of question
                        String subject="";
                        //Client enters publisher mode::
                        ZMQ.Socket publisher = context.socket(SocketType.PUSH);
                        //Connect to ip
                        publisher.connect("tcp://localhost:5555");
                        //Get Question from Client
                        System.out.println("Select the following subject for the question:");
                        System.out.println("-------------------------\n");
                        System.out.println("1 - Science");
                        System.out.println("2 - Math");
                        System.out.println("3 - English");
                        System.out.println("4 - Quit\n");
                        //Get user input
                        userInput = stdIn.readLine();

                        switch (userInput) {
                            case "1":{
                                subject= "Science";
                                //Get Question from client
                                System.out.println("Enter the Science Question:\n");
                                userInput=stdIn.readLine();
                                question=userInput;
                                break;
                            }
                            case "2":{
                                subject= "Math";
                                //Get Question from client
                                System.out.println("Enter the Math Question:\n");
                                userInput=stdIn.readLine();
                                question=userInput;
                                break;
                            }
                            case "3":{
                                subject= "English";
                                //Get Question from client
                                System.out.println("Enter the English Question:\n");
                                userInput=stdIn.readLine();
                                question=userInput;
                                break;
                            }
                                
                            case "4":{
                                break;
                            }
                        }
                        //Add their question to database to be viewed when they wish to
                        Connection db = DatabaseConnection.getConnection();
                        // Insert Query
                        String insertQuery = "INSERT INTO clientquestions (userName, question, subject) VALUES (?, ?, ?)";
                        PreparedStatement preparedStatement = db.prepareStatement(insertQuery); {
                            preparedStatement.setString(1, userName);
                            preparedStatement.setString(2, question);
                            preparedStatement.setString(3, subject);
                        }
                        // Execute the insert query
                        preparedStatement.executeUpdate();
                        db.close();
                        //Combine the question with subject by delimeter
                        question = question + "/" + subject;
                        //Send to queue
                        byte[] questionByteForm = question.getBytes(Charset.forName("UTF-8"));
                        //Send task to task queue
                        publisher.send(questionByteForm);
                        //Notfiy user the question is sent
                        System.out.println("Your question has been sent, Thanks!");
                        break;
                    }
                    //Get Quiz Questions
                    case "2":{
                        //Client enter worker mode
                        ZContext context2 = new ZContext();
                        //Create Sockets
                        ZMQ.Socket worker = context2.createSocket(SocketType.PULL);
                        ZMQ.Socket requestSocket = context2.createSocket(SocketType.PUSH);
                        //Bindings
                        worker.connect("tcp://localhost:5557");
                        requestSocket.connect("tcp://localhost:5556");
                        //Get the amount of questions to recieve from client
                        String numOfQuestion;
                        String subject;
                        String data;
                        System.out.println("Enter the Amount of Questions To Retrieve:");
                        numOfQuestion = stdIn.readLine();
                        //Get subject of question
                        System.out.println("Select the following subject for the all questions:");
                        System.out.println("-------------------------\n");
                        System.out.println("1 - Science");
                        System.out.println("2 - Math");
                        System.out.println("3 - English");
                        System.out.println("4 - Quit\n");
                        userInput=stdIn.readLine();
                        switch (userInput) {
                            case "1":{
                                subject="Science";
                                data=numOfQuestion+"/"+subject;
                                int recievedQuestions=0;
                                //Send the request for the questions
                                requestSocket.send(data,0);

                                //Recieve Questions
                                System.out.println("Waiting to recieve questions from queue...\n");
                                while(recievedQuestions != Integer.parseInt(numOfQuestion)){
                                    byte[] questionFromQueue = worker.recv(ZMQ.NOBLOCK);
                                    if(questionFromQueue !=null){
                                        System.out.println(subject+" Question: "+ new String(questionFromQueue, ZMQ.CHARSET)+"\n");
                                        recievedQuestions++;
                                    }
                                }
                                Thread.sleep(1000);
                                break;
                            }
                            case "2":{
                                subject="Math";
                                data=numOfQuestion+"/"+subject;
                                int recievedQuestions=0;
                                //Send the request for the questions
                                requestSocket.send(data,0);

                                //Recieve Questions
                                System.out.println("Waiting to recieve questions from queue...\n");
                                while(recievedQuestions != Integer.parseInt(numOfQuestion)){
                                    byte[] questionFromQueue = worker.recv(ZMQ.NOBLOCK);
                                    if(questionFromQueue !=null){
                                        System.out.println(subject+" Question: "+ new String(questionFromQueue, ZMQ.CHARSET)+"\n");
                                        recievedQuestions++;
                                    }
                                }
                                Thread.sleep(1000);
                                break;
                            }
                            case "3":{
                                subject="English";
                                data=numOfQuestion+"/"+subject;
                                int recievedQuestions=0;
                                //Send the request for the questions
                                requestSocket.send(data,0);

                                //Recieve Questions
                                System.out.println("Waiting to recieve questions from queue...\n");
                                while(recievedQuestions != Integer.parseInt(numOfQuestion)){
                                    byte[] questionFromQueue = worker.recv(ZMQ.NOBLOCK);
                                    if(questionFromQueue !=null){
                                        System.out.println(subject+" Question: "+ new String(questionFromQueue, ZMQ.CHARSET)+"\n");
                                        recievedQuestions++;
                                    }
                                }
                                Thread.sleep(1000);
                                break;
                            }
                            case "4":{
                                break;
                            }
                                
                        }
                    }
                    //Generate Random Test
                    case "3":{

                    }
                    //View Own Submitted Questions
                    case "4":{
                        try {
                            Connection db = DatabaseConnection.getConnection();
                            // Query
                            String selectQuery = "SELECT question, subject FROM clientquestions WHERE userName = ?";
                            //Prepare statement
                            PreparedStatement preparedStatement = db.prepareStatement(selectQuery); {
                                preparedStatement.setString(1, userName);
                                ResultSet resultSet = preparedStatement.executeQuery(); {
                                    System.out.println("Your Submitted Questions: \n");
                                    while (resultSet.next()) {
                                        // Retrieve question and subject from the result set
                                        String question = resultSet.getString("question");
                                        String subject = resultSet.getString("subject");
                                        // Print questions
                                        System.out.println("Question: " + question);
                                        System.out.println("Subject: " + subject);
                                        System.out.println("------");
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        Thread.sleep(1000);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}