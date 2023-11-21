import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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
            //Login User
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
                System.out.println("3 - Generate a random test based on a subject");
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
                            }
                            case "3":{
                                subject= "English";
                                //Get Question from client
                                System.out.println("Enter the English Question:\n");
                                userInput=stdIn.readLine();
                                question=userInput;
                            }
                                
                            case "4":{
                                break;
                            }
                        }
                        //Combine the question with subject by delimeter
                        question = question + "/" + subject;
                        //Send to queue
                        byte[] questionByteForm = question.getBytes(Charset.forName("UTF-8"));
                        //Send task to task queue
                        publisher.send(questionByteForm);
                        //Notfiy user the question is sent
                        System.out.println("Your question has been sent, Thanks!");
                    }
                }
            }
            

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}