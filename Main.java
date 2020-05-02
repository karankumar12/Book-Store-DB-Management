import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static java.lang.System.exit;

public class main {

    public static Character displayInitialMenu(Scanner sc){
        System.out.println("***********************************************************************");
        System.out.println("***\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t***");
        System.out.println("***\t\t\t\tWelcome to the Online Book Store\t\t\t\t\t***");
        System.out.println("***\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t***");
        System.out.println("***********************************************************************");
        System.out.println("1. Member Login\n" + "2. New Member Registration\n" + "q. Quit\n");

        return sc.next().charAt(0);
    }
    public static void displayTwoRecords(ResultSet rs2) throws Exception{
        int displayed = 0;
        while(displayed < 2 && rs2.next()){
            System.out.println("Author: " + rs2.getString("author"));
            System.out.println("Title: " + rs2.getString("title"));
            System.out.println("ISBN: "+ rs2.getString("isbn"));
            System.out.println("Price: " + rs2.getDouble("price"));
            System.out.println();
            displayed++;
        }
    }
    public static int getCount(Connection conn, String query) throws Exception{
        Statement st = conn.createStatement();
        ResultSet rs3 = st.executeQuery(query);
        int count = 0;
        if(rs3.next()){
            count = rs3.getInt("count");
        }
        return count;

    }
    public static void BrowseBySubject(Scanner sc, Connection conn, String userId) throws Exception {
        String query = "Select Distinct Subject from Books";
        Statement st = conn.createStatement();
        Statement st2 = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        int i = 0;
        List<String> subjects = new ArrayList<>();
        while(rs.next()){
            String subject = rs.getString("subject");
            System.out.println(i+1+ ". "+ subject);
            i++;
            subjects.add(subject);
        }

        System.out.println("Enter Your Choice: ");
        int choice = sc.nextInt();
        String query2 = "Select * from books where subject= \'" + subjects.get(choice-1)+ "\'";
        String query3 = "Select count(*) as count from books where subject= \'"+ subjects.get(choice-1) + "\'";

        ResultSet rs2 = st.executeQuery(query2);
        int count = getCount(conn, query3);
        System.out.println(count + " Books available on this subject.");

        displayTwoRecords(rs2);

        do {
            System.out.println("Enter ISBN to add to Cart or \n" +
                    "n Enter to continue to browse or \n" +
                    "ENTER to go back to menu: n\n");
            String choice2 = sc.next();
            if (choice2.equals("\\n")) {
                DisplayMemberMenu(sc, conn, userId);
            } else if (choice2.equals("n")) {
                displayTwoRecords(rs2);
            } else {
                System.out.println("Please enter the quantity: ");
                int quantity = sc.nextInt();
                String addISBN = "insert into cart values (\'" + userId + "\',\'" + choice2 + "\', \'" + quantity+"\')";
                st.executeUpdate(addISBN);
                return;
            }
        }while(true);

    }

    public static void SearchBy(Scanner sc, Connection conn, String userId) throws Exception{
        int choice;
        System.out.println("1. Author Search\n" +
                "2. Title Search\n" +
                "3. Go Back to Member Menu\n");
        choice = sc.nextInt();
        String query = "";
        String countQuery = "";
        switch(choice){
            case 1:
                System.out.println("Enter author name or part of name: ");
                String author = sc.next();
                query = "Select * from books where Author Like \'%" + author +"%\'";
                countQuery = "Select count(*) as count from books where Author Like \'%" + author +"%\'"; break;
            case 2:
                System.out.println("Enter title or part of title:");
                String title = sc.next();
                query = "Select * from books where Title Like \'%" + title +"%\'";
                countQuery = "Select count(*) as count from books where Title Like \'%" + title +"%\'"; break;
            case 3:
                DisplayMemberMenu(sc, conn, userId); exit(0);
            default:
                System.out.println("Sorry you entered the wrong choice");
                exit(0);

        }
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        int count = getCount(conn, countQuery);

        System.out.println(count + " books found.");
        displayTwoRecords(rs);

        do {
            System.out.println("Enter ISBN to add to Cart or \n" +
                    "n Enter to continue to browse or \n" +
                    "ENTER to go back to menu: n\n");
            String choice2 = sc.next();

            if (choice2.equals("\n")) {
                DisplayMemberMenu(sc, conn, userId);
            } else if (choice2.equals("n")) {
                displayTwoRecords(rs);
            } else {
                System.out.println("Please enter the quantity: ");
                int quantity = sc.nextInt();
                String addISBN = "insert into cart values (\'" + userId + "\',\' " + choice2 + "\', \'" + quantity+"\')";
                st.executeUpdate(addISBN);
                return;
            }
        }while(true);



    }
    public static void displayCart(Connection conn, String userId) throws Exception{
        System.out.println("---------------------------------------------------------------------------------");
        System.out.print(String.format("%10s","ISBN"));
        System.out.print(String.format("%30s","Title"));
        System.out.print(String.format("%10s","$"));
        System.out.print(String.format("%10s","Qty"));
        System.out.print(String.format("%10s","Total"));
        System.out.println();
        System.out.println("---------------------------------------------------------------------------------");

        String query = "Select cart.isbn, books.title, qty, price from books, cart Where cart.isbn = books.isbn"; //AND cart.userid = \'"+userId+"\'";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);
        double sum = 0;
        while(rs.next()){
            System.out.print(String.format("%10s", rs.getString("isbn")));
            System.out.print(String.format("%30s",rs.getString("title")));
            System.out.print(String.format("%10s", rs.getString("price")));
            System.out.print(String.format("%10s", rs.getString("qty")));
            sum += rs.getDouble("qty")*rs.getDouble("price");
            double cost = rs.getDouble("qty")*rs.getDouble("price");
            System.out.print(String.format("%10s", cost + " "));
            System.out.println();
        }
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("Total =" + String.format("%62s",sum));

    }
    public static void ModifyCart(Scanner sc, Connection conn, String userId) throws Exception {

        do {
            displayCart(conn, userId);
            System.out.println("Enter d to delete item\n" +
                    "e to edit cart or\n" +
                    "q to go back to Menu: d\n");
            Character choice = sc.next().charAt(0);
            String query2 = "";
            String isbn;
            Statement st2 = conn.createStatement();
            switch (choice) {
                case 'd':
                    System.out.println("Enter isbn of the item");
                    isbn = sc.next();
                    query2 = "Delete from cart where isbn=\'" + isbn + "\'";
                    st2.executeUpdate(query2);
                    break;
                case 'e':
                    System.out.println("Enter isbn of the item");
                    isbn = sc.next();
                    System.out.println("Enter new quantity of item: ");
                    int newqty = sc.nextInt();
                    query2 = "Update cart Set qty = \'" + newqty + "\' where isbn = \'" + isbn + "\'";
                    st2.executeUpdate(query2);
                    break;
                case 'q':
                    DisplayMemberMenu(sc, conn, userId);
                    exit(0);
                default:
                    System.out.println("Wrong choice entered.");
                    exit(0);

            }
        }while(true);

    }

    public static void displayOrderDetails(Connection conn, ResultSet rs3, String name, String address, String city, String stateZip) throws Exception{
        if(rs3.next()) {
            System.out.println("\t\t\tDetails for Order no." + rs3.getString("ono"));
            System.out.println("Shipping Address \t\t\t\t\t\tBilling address");
            System.out.println("Name:" + name + "\t\t\t\t\tName: " + name);
            System.out.println("Address: " + address + "\t\tAddress: " + address);
            System.out.println(city + "\t\t\t" + city);
            System.out.println(stateZip + "\t\t\t" + stateZip);

            System.out.println("---------------------------------------------------------------------------------");
            System.out.print(String.format("%10s","ISBN"));
            System.out.print(String.format("%30s","Title"));
            System.out.print(String.format("%10s","$"));
            System.out.print(String.format("%10s","Qty"));
            System.out.println(String.format("%10s","Total"));
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println();
            Double sum = 0.0;
            do {

                Double total = 0.0;
                String getBook = "select * from books where isbn=\'" + rs3.getString("isbn") + "\'";

                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(getBook);
                total = rs3.getDouble("price") * rs3.getDouble("qty");
                sum += total;
                if (rs.next()){
                    System.out.print(String.format("%10s", rs3.getString("isbn")));
                    System.out.print(String.format("%30s",rs.getString("title")));
                    System.out.print(String.format("%10.2f", rs3.getDouble("price")));
                    System.out.print(String.format("%10s", rs3.getInt("qty")));
                    System.out.println(String.format("%10.2f", rs3.getInt("qty")*rs3.getDouble("price")));
                }
                //System.out.println(rs3.getString("isbn") + "\t\t\t" + rs.getString("title") + "\t\t" + rs3.getString("price") + "    " + rs3.getString("qty") + "    " + total);
            } while (rs3.next());
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("Total =" + String.format("%62.2f",sum));
            return;
        }
    }
    public static void CheckStatus(Scanner sc,Connection conn, String userId) throws Exception{

        String getName = "Select * from members where userId = \'"+userId+"\'";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(getName);
        String name = "";
        if(rs.next()){
            name = rs.getString("fname")+" "+rs.getString("lname");
            System.out.println("Order place by: "+ name);
        }
        System.out.println("-----------------------------------------------------------\n" +
                "ORDER NO \tRECEIVED DATE \t\tSHIPPED DATE\n" +
                "-----------------------------------------------------------\n");
        String getOrders = "Select * from orders where userId=\'"+userId+"\'";
        Statement st2 = conn.createStatement();
        ResultSet rs2 = st2.executeQuery(getOrders);
        String address = "", city = "", stateZip = "";
        while(rs2.next()){
            address = rs2.getString("shipAddress");
            city = rs2.getString("shipCity");
            stateZip = rs2.getString("shipState") + " " + rs2.getString("shipZip");
            System.out.println(rs2.getString("ono")+"\t\t"+rs2.getString("received")+"\t"+rs2.getString("shipped"));
        }
        System.out.println("-----------------------------------------------------------");
        System.out.println("Enter the Order No to display its details or (q) to quit: ");
        String number = sc.next();

        if(number.equals("q")){
            DisplayMemberMenu(sc, conn, userId);
        }
        else{
            String getOrderDets = "Select * from odetails where ono= \'"+number+"\'";
            Statement st3 = conn.createStatement();
            ResultSet rs3 = st3.executeQuery(getOrderDets);
            displayOrderDetails(conn, rs3, name, address, city, stateZip);
            DisplayMemberMenu(sc, conn, userId);
        }

    }

    public static int generateOrderNumber(String userId, Connection conn) throws SQLException {
        String getOrderNumbers = "select * from orders";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(getOrderNumbers);
        HashMap<Integer, Boolean> map = new HashMap<>();
        while (rs.next()) {
            if (!map.containsKey(rs.getInt("ono"))) {
                map.put(rs.getInt("ono"), true);
            }
        }
        int orderNum = 1;
        while (true) {
            if (map.containsKey(orderNum)) {
                orderNum++;
            } else
                return orderNum;
        }
    }
    public static void OneClickCheckOut(Scanner sc, Connection conn, String userId ) throws  Exception{
        String getCart = "Select * from cart where userId = \'" +userId + "\'";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(getCart);

        if(rs.next()){
            String address = "";
            String city ="";
            String state = "";
            int zipcode = 0;
            String name = "";
            int orderNum = generateOrderNumber(userId, conn);
            System.out.println("\t\t\t\tInvoice for order Number. "+orderNum);
            String getMemberDetails = "select * from members where userId = \'" + userId + "\'";

            Statement st2 = conn.createStatement();
            ResultSet rs3 = st2.executeQuery(getMemberDetails);
            if(rs3.next()){
                address =rs3.getString("address");
                city = rs3.getString("city");
                state = rs3.getString("state");
                zipcode = rs3.getInt("zip");
                name = rs3.getString("fname")+" "+ rs3.getString("lname");

            }
            else{
                System.out.println("Member does not have details.");
            }
            System.out.println("Shipping address" + String.format("%40s","Billing Address"));
            System.out.println("Name: "+name+String.format("%40s", "Name: "+ name));
            System.out.println("Address: "+address+String.format("%40s", "Address: "+address));
            System.out.println(city+String.format("%40s", city));
            System.out.println(state + zipcode+ String.format("%40s", state+ " " +Integer.toString(zipcode)));

            System.out.println("-----------------------------------------------------------------------------");
            System.out.print(String.format("%10s","ISBN"));
            System.out.print(String.format("%30s","Title"));
            System.out.print(String.format("%10s","$"));
            System.out.print(String.format("%10s","Qty"));
            System.out.println(String.format("%10s","Total"));
            System.out.println("-----------------------------------------------------------------------------");
            double sum = 0;
            do{
                String getPrice = "select * from books where isbn=\'"+rs.getString("isbn")+"\'";
                Statement s3 = conn.createStatement();
                ResultSet book = s3.executeQuery(getPrice);
                if(book.next()) {
                    String insertIntoDetails = "insert into odetails values (\'" + orderNum + "\',\'" + rs.getString("isbn") + "\',\'" + rs.getInt("qty") + "\',\'" + book.getDouble("price") + "\')";
                    Statement insert = conn.createStatement();
                    insert.executeUpdate(insertIntoDetails);

                }

                System.out.print(String.format("%10s",rs.getString("isbn")));

                System.out.print(String.format("%30s",book.getString("title")));
                System.out.print(String.format("%10s",book.getString("price")));
                System.out.print(String.format("%10s",rs.getString("qty")));
                System.out.println(String.format("%10.2f",book.getDouble("price")*rs.getInt("qty")));
                sum += book.getDouble("price")*rs.getInt("qty");
            }while(rs.next());
            String insertIntoOrder = "insert into orders values (\'"+userId+"\',\'"+orderNum+"\', \"2001-01-01\", \"2001-01-01\", \'"+address+"\',\'"+city+"\',\'"+state+"\',\'"+zipcode+"\')";
            System.out.println("-----------------------------------------------------------------------------");
            System.out.println("Total =" + String.format("%62.2f",sum));
            Statement insert = conn.createStatement();
            insert.executeUpdate(insertIntoOrder);

            String deleteFromCart = "delete from cart where userId = \'"+userId +"\'";
            insert.executeUpdate(deleteFromCart);



        }
        else {
            System.out.println("No items in the cart.");
            DisplayMemberMenu(sc, conn, userId);
        }
    }

    public static void displayInfo(Connection conn, String userId) throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("select * from members where userid =\'"+userId+"\'");
        if(rs.next()){
            System.out.println("Name: " + rs.getString("fname")+" "+rs.getString("lname"));
            System.out.println("Address: "+ rs.getString("address"));
            System.out.println("City: "+ rs.getString("city"));
            System.out.println("Zipcode: "+rs.getString("zip"));
            System.out.println("Phone Number: "+rs.getString("phone"));
            System.out.println("Email: "+ rs.getString("email"));
            System.out.println("UserId: "+ rs.getString("userid"));
        }

    }
    public static void MemberMenu(char choice, Scanner sc, Connection conn, String userId) throws Exception{
        switch(choice){
            case '1':
                BrowseBySubject(sc, conn, userId);
                break;
            case '2':
                SearchBy(sc, conn, userId);
                break;
            case '3':
                ModifyCart(sc, conn, userId);
                break;
            case '4':
                CheckStatus(sc, conn, userId);
                break;
            case '5': OneClickCheckOut(sc, conn, userId);
                break;
            case '6': OneClickCheckOut(sc, conn, userId);
                break;
            case '7':
                displayInfo(conn, userId);
                break;
            case '8':
                System.out.println("You have successfully logged out.");
                exit(0);
                break;
        }
    }
    public static void DisplayMemberMenu(Scanner sc, Connection conn, String userId) throws Exception{
        Character choice;
        do {
            System.out.println("***********************************************************************");
            System.out.println("***\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t***");
            System.out.println("***\t\t\t\tWelcome to the Online Book Store\t\t\t\t\t***");
            System.out.println("***\t\t\t\t           Member Menu          \t\t\t\t\t***");
            System.out.println("***\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t***");
            System.out.println("***********************************************************************");
            System.out.println("1. Browse by Subject\n" +
                    "2. Search by Author/Title/Subject\n" +
                    "3. View/Edit Shopping Cart\n" +
                    "4. Check Order Status\n" +
                    "5. Check Out\n" +
                    "6. One Click Check Out\n" +
                    "7. View/Edit Personal Information\n" +
                    "8. Logout\n");
            choice = sc.next().charAt(0);
            MemberMenu(choice, sc, conn, userId);
        }while(choice != 8);

    }

    public static void makeNewMember(Connection conn, Scanner sc) throws Exception{
        System.out.println("\t New Member Registration");
        System.out.println("Enter First Name");
        String firstName = sc.next();

        System.out.println("Enter last name");
        String lastName = sc.next();

        System.out.println("Enter address:");
        String address = sc.next();
        address += sc.nextLine();

        System.out.println("Enter city:");
        String city =sc.next();

        System.out.println("Enter State:");
        String state = sc.next();

        System.out.println("Enter Zip:");
        int Zip = sc.nextInt();

        System.out.println("Enter Phone:");
        String phone = sc.next();

        System.out.println("Enter Email-address:");
        String email = sc.next();

        System.out.println("Enter User-Id: ");
        String userId = sc.next();

        System.out.println("Enter password: ");
        String password = sc.next();

        System.out.println("Do you wush to store credit card information(y/n)");
        Character choice = sc.next().charAt(0);

        String type = "";
        String number = "";
        if(choice == 'y'){
            System.out.println("Enter type of credit card(amex/visa):");
            type = sc.next();
            do {
                System.out.println("Enter credit card number");
                number = sc.next();
            }while(number.length() != 16);

        }

        Statement st = conn.createStatement();
        String insert = "insert into members values (\'" + firstName + "\',\'" + lastName + "\', \'" + address+ "\', \'"+ city + "\', \'" + state + "\'," + Zip + ", \'" + phone + "\', \'" + email + "\', \'" + userId + "\', \'" + password + "\', \'" + type+ "\', \'" + number + "\')";
        System.out.println(insert);
        st.executeUpdate(insert);

    }
    public static String Login(Connection conn, Scanner sc) throws Exception{

        //Asks for userId and Password
        System.out.println("Enter UserId: ");
        String userId = sc.next();
        System.out.println("Enter Password: ");
        String password = sc.next();
        String query = "Select * From members " + "Where userid = \'" + userId + "\'";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(query);


        //If the userId exists in the database
        if(rs.next()){

            //Compares to see if the password matches, else asks for the password repeatedly.
            String userPassword = rs.getString("password");
            while(!password.equals(userPassword)){
                System.out.println("You entered the wrong password. Please enter again.");
                password = sc.next();
            }
        }
        else{
            System.out.println("The userId does not exist");
            userId = Login(conn, sc);

        }
        st.close();
        return userId;



    }
    public static void main(String[] args) throws ClassNotFoundException{

        try{
            Scanner sc = new Scanner(System.in);
            Character choice = displayInitialMenu(sc);

            //String myDriver = "com.mysql.jdbc.Driver";
            String myUrl = "Database url";
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection(myUrl, "root", "password");
            Statement st = conn.createStatement();

            while(choice != '1' && choice != '2' && choice != 'q'){
                System.out.println("Sorry, you entered the wrong choice. Please enter again");
                choice = sc.next().charAt(0);
            }

            //If user chooses to login
            if(choice == '1'){

                //Function to log in that returns the userId
                String userId = Login(conn, sc);

                //Displays the member menu
                DisplayMemberMenu(sc, conn, userId);
            }
            else if(choice == '2'){
                //Function to create a new member in the members table
                makeNewMember(conn, sc);
            }
            else{

            }
            //st.close();

        }catch(Exception e){
            System.err.println(e.getClass());
            System.err.println(e.getMessage());
        }
    }
}
