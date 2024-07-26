/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      System.out.println();
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      System.out.println();
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            int authorisedUser = -1;
            String userType = "customer";
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != -1) {
              boolean usermenu = true;
              while(usermenu) {
                // User Type checked on every refresh of the menu screen for partially realistic security
                userType = verifyUserType(esql, authorisedUser);
                
                System.out.println("\nMAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Stores within 30 miles");
                
                switch (userType) {
                    case "customer":
                        System.out.println("2. View Product List");
                        System.out.println("3. Place an Order");
                        System.out.println("4. View 5 Most Recent Orders");
                        System.out.println(".........................");
                        System.out.println("20. Log Out");
                        
                        switch (readChoice()) {
                            case 1: viewStores(esql, authorisedUser); break;
                            case 2: viewProducts(esql); break;
                            case 3: placeOrder(esql, authorisedUser); break;
                            case 4: viewRecentOrders(esql, authorisedUser); break;

                            case 20: usermenu = false; break;
                            default: System.out.println("Unrecognized choice."); break;
                        }

                        break;
                    case "manager":
                        System.out.println("2. View Product List");
                        System.out.println("3. Update Product");
                        System.out.println("4. View 5 Most Recent Product Updates");
                        System.out.println("5. View 5 Most Popular Items");
                        System.out.println("6. View 5 Most Popular Customers");
                        System.out.println("7. Place Product Supply Request to Warehouse");
                        System.out.println("8. View Orders");
                        System.out.println(".........................");
                        System.out.println("20. Log Out");

                        switch (readChoice()) {
                            case 1: viewStores(esql, authorisedUser); break;
                            case 2: viewProducts(esql); break;
                            case 3: updateProduct(esql, authorisedUser, 0); break;
                            case 4: viewRecentUpdates(esql, authorisedUser); break;
                            case 5: viewPopularProducts(esql, authorisedUser); break;
                            case 6: viewPopularCustomers(esql, authorisedUser); break;
                            case 7: placeProductSupplyRequests(esql, authorisedUser); break;
                            case 8: viewStoreOrders(esql, authorisedUser); break;

                            case 20: usermenu = false; break;
                            default: System.out.println("Unrecognized choice."); break;
                        }

                        break;
                    case "admin":
                        System.out.println("2. View/Update User Information");
                        System.out.println("3. View/Update Product");
                        System.out.println(".........................");
                        System.out.println("20. Log Out");

                        switch (readChoice()) {
                            case 1: viewStores(esql, authorisedUser); break;
                            case 2: adminUpdateUser(esql); break;
                            case 3: updateProduct(esql, authorisedUser, 1); break;

                            case 20: usermenu = false; break;
                            default: System.out.println("Unrecognized choice."); break;
                        }

                        break;

                    default: break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Amazon esql){
      try{
         String name = "";
         String password = "";
         String latitude = "";
         String longitude = "";

         do {
            System.out.print("\tEnter name (less than 50 characters): ");
            name = in.readLine();
         } while (!nameUnique(esql, name) || name.length() > 50);
         
         do {
            System.out.print("\tEnter password consisting of at least 3 and no more than 11 characters: ");
            password = in.readLine();
         } while (password.length() < 3 || password.length() > 11);
    
         do {
            System.out.print("\tEnter latitude (-90 to 90, up to six digits after decimal point): ");   
            latitude = in.readLine();
         } while (!validateLocationInput(latitude, true));

         do {
            System.out.print("\tEnter longitude (-180 to 180, up to six digits after decimal point): ");
            longitude = in.readLine();
         } while (!validateLocationInput(longitude, false));

         String type="customer";

	 String query = String.format("INSERT INTO USERS (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static int LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE name = '%s' AND password = '%s'", name, password);
         List<List<String>> userInfo = esql.executeQueryAndReturnResult(query);
	 if (!userInfo.isEmpty()) { // Return userID if user exists
	    return Integer.parseInt(userInfo.get(0).get(0));
         }
         System.out.println("\nUnrecognized username or incorrect password entered.\n");
         return -1;
      }catch(Exception e){
         System.err.println(e.getMessage());
         return -1;
      }
   }//end

// Rest of the functions definition go in here

   /*
    * Check user type (customer, manager, admin), based on userID from logged in user
    * @return String representing user type
    **/
   public static String verifyUserType(Amazon esql, int userID) {
        try{
            String query = String.format("SELECT type FROM USERS WHERE userID = '%s'", String.valueOf(userID));
            String result = (esql.executeQueryAndReturnResult(query)).get(0).get(0).trim();
            return result;
        }catch(Exception e){
            System.err.println(e.getMessage());
            return null;
        }
    }

    /*
     * Check if a name already exists in the database, print statement if it does
     * @return boolean confirming name is or isn't unique
     **/

    public static boolean nameUnique(Amazon esql, String name) {
        try{
            String query = String.format("SELECT * FROM USERS WHERE name = '%s'", name);
            if (esql.executeQuery(query) == 0) {
                return true;
            }
            else {
                System.out.println("That name has already been taken. Please choose another.");
                return false;
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    /*
     * Check if input string is a positive number and return the parsed value. Print error message and return otherwise.
     * @return int form of entered String
     **/

    public static int parseIntCheck(String s) {
        try{
            int value = Integer.parseInt(s);
            if (value < 0) {
                System.out.println("Invalid input. This field should be a positive number.");
                return -1;
            }
            else { return value; }
        }catch(NumberFormatException e){
            System.out.println("Invalid input. This field should be a number.");
            return -1;
        }
    }

    /*
     * Same as above but for float values
     * @return float form of entered string
     **/

    public static float parseFloatCheck(String s) {
        try{
            float value = Float.parseFloat(s);
             if (value < 0) {
                System.out.println("Invalid input. This field should be a positive number.");
                return -1;
            }
            else { return value; }
        }catch(NumberFormatException e){
            System.out.println("Invalid input. This field should be a number.");
            return -1;
        }

    }

    /*
     * Check if input value exists in the database
     * @return boolean value representing existence
     **/

    public static boolean verifyInput(Amazon esql, String table, String column, String input) {
        try{
            String query = String.format("SELECT * FROM %s WHERE %s = '%s'", table, column, input);
            if (esql.executeQuery(query) != 0) { return true; }
            else { 
                System.out.println("Invalid input. Entered value does not exist in database.");
                return false; } // Shouldn't reach this one
        }catch(SQLException e){
            System.out.println("Invalid input. Entered value does not exist in database.");
            return false;
        }
    }

    /*
     *  Check if a manager works at a store
     *  @return boolean representing whether manager works at input store ID
     **/

    public static boolean verifyManager(Amazon esql, int managerID, int storeID) {
        try{
            String query = String.format("SELECT * FROM Store WHERE storeID = '%d' AND managerID = '%d'", storeID, managerID);
            if (esql.executeQuery(query) != 0) { return true; }
            else {
                System.out.println("Invalid input. You do not manage this store");
                return false; 
            }
        }catch(SQLException e){
            System.out.println("Invalid input. You do not manage this store.");
            return false;
        }
    }

    /*
     *  Check if a given store has a given product
     *  @return boolean representing if store has the product
     **/

    public static boolean checkStore(Amazon esql, int storeID, String productName) {
        try{
            String query = String.format("SELECT * FROM Product WHERE storeID = '%d' AND productName = '%s'", storeID, productName);
            if (esql.executeQuery(query) != 0) { return true; }
            else { return false; }
        }catch(SQLException e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    /*
     *  Find the price of a productName at the store closest to the one with storeID
     *  @return float representing the price found
     **/
    
    public static float getNearestPrice(Amazon esql, int storeID, String productName) {
        try{
            // Get location of given store
            String query = String.format("SELECT latitude, longitude FROM Store WHERE storeID = '%d'", storeID);
            List<List<String>> givenStore = esql.executeQueryAndReturnResult(query);
            double storeLat = Double.parseDouble(givenStore.get(0).get(0));
            double storeLon = Double.parseDouble(givenStore.get(0).get(1));

            // Find other stores selling product
            query = String.format("SELECT s.latitude, s.longitude, p.pricePerUnit FROM Store s JOIN Product p ON s.storeID = p.storeID WHERE s.storeID != '%d' AND p.productName = '%s'", storeID, productName);
            List<List<String>> otherStores = esql.executeQueryAndReturnResult(query);
            double currDistance = 0;
            double minDistance = Integer.MAX_VALUE;
            float nearestPrice = 0;

            // Check which store has the closest distance to the given store
            for (int i = 0; i < otherStores.size(); ++i) {
                currDistance = esql.calculateDistance(storeLat, storeLon, Double.parseDouble(otherStores.get(i).get(0)), Double.parseDouble(otherStores.get(i).get(1)));
                if (currDistance < minDistance) {
                    minDistance = currDistance;
                    nearestPrice = Float.parseFloat(otherStores.get(i).get(2));
                }
            }

            return nearestPrice;
        }catch(Exception e){
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public static boolean validateLocationInput(String location, boolean latitude) {
        String regex = "";
        
        if (latitude) { regex = "^[-+]?([1-8]?\\d(\\.\\d{1,6}+)?|90(\\.0{1,6}+)?)$"; }
        else { regex = "^[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d{1,6}+)?)$"; }

        if (location.matches(regex)) { return true; }
        else {
            if (latitude) { System.out.println("Invalid input. Your latitude coordinate should be in the range of -90 to 90, with up to six digits after the decimal point."); }
            else { System.out.println("Invalid input. Your latitude coordinate should be in the range of -180 to 180, with up to six digits after the decimal point."); }
            
            return false;
        }
    }

   public static void viewStores(Amazon esql, int authorisedUser) {
        try{         
            List<List<String>> userLocation = esql.executeQueryAndReturnResult(String.format("SELECT latitude, longitude FROM Users WHERE userID = '%d'", authorisedUser));
            double userLat = Double.parseDouble(userLocation.get(0).get(0));
            double userLong = Double.parseDouble(userLocation.get(0).get(1));
            List<List<String>> storeList = esql.executeQueryAndReturnResult("SELECT storeID, latitude, longitude FROM Store");
            boolean storesFound = false;
            System.out.print("storeID\tdistance (miles)");
            System.out.println();
            for (int i = 0; i < storeList.size(); ++i) {
                double distance = esql.calculateDistance(userLat, userLong, Double.parseDouble(storeList.get(i).get(1)), Double.parseDouble(storeList.get(i).get(2))); 
                if (distance <= 30) {
                    System.out.print(String.format("%s\t%.2f", storeList.get(i).get(0), distance));
                    System.out.println();
                    storesFound = true;
                }
            }
            
            if (!storesFound) {
                System.out.print("No stores found within 30 miles of your location.");
                System.out.println();
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

   public static void viewProducts(Amazon esql) {
        try {
            boolean valid = false;
            String input = "";
            int storeID = -1;

            do {
                System.out.print("Enter the ID of the store to view products at (no entry to cancel): ");
                input = in.readLine();
                if (input.isEmpty()) { storeID = -1; valid = true; }
                else {
                    storeID = parseIntCheck(input);
                    if (storeID != -1) { valid = verifyInput(esql, "Store", "storeID", input); }
                }
            } while (!valid);

            if (storeID != -1) {
                esql.executeQueryAndPrintResult(String.format("SELECT productName, numberOfUnits, pricePerUnit FROM Product WHERE storeID = '%d'", storeID));
            }
        }catch(Exception e){
            System.err.println(e.getMessage());   
        }
    }

   public static void placeOrder(Amazon esql, int authorisedUser) {
        try {
            List<List<String>> userLocation = esql.executeQueryAndReturnResult(String.format("SELECT latitude, longitude FROM Users WHERE userID = '%d'", authorisedUser));
            double userLat = Double.parseDouble(userLocation.get(0).get(0));
            double userLong = Double.parseDouble(userLocation.get(0).get(1));
            List<List<String>> storeList = esql.executeQueryAndReturnResult("SELECT storeID, latitude, longitude FROM Store");
            List<String> foundStores = new ArrayList<String>();
            boolean storesFound = false;
            boolean valid = false;
            String input = "";
            int storeID = -1;
            String orderProduct = "";
            int availableUnits = -1;
            int orderCount = -1;

            System.out.print("storeID\tdistance (miles)");
            System.out.println();
            for (int i = 0; i < storeList.size(); ++i) {
                double distance = esql.calculateDistance(userLat, userLong, Double.parseDouble(storeList.get(i).get(1)), Double.parseDouble(storeList.get(i).get(2))); 
                if (distance <= 30) {
                    System.out.print(String.format("%s\t%.2f", storeList.get(i).get(0), distance));
                    System.out.println();
                    foundStores.add(storeList.get(i).get(0));
                    storesFound = true;
                }
            }
            
            if (storesFound) {
                do {
                    System.out.print("\nEnter the ID of the store within 30 miles you will order from (no entry to cancel): ");
                    input = in.readLine();
                    
                    if (input.isEmpty()) { storeID = -1; valid = true; }
                    else {
                        storeID = parseIntCheck(input);
                        if (storeID != -1) {
                            valid = foundStores.contains(input);
                            if (!valid) { System.out.println("Please enter a store ID from the provided list of stores within 30 miles of your location."); }
                        }
                    }
                } while (!valid);

                if (storeID != -1) {
                    esql.executeQueryAndPrintResult(String.format("SELECT productName, numberOfUnits, pricePerUnit FROM Product WHERE storeID = '%d'", storeID));
                    
                    do {
                        System.out.print("Enter the name of the product you are ordering from store " + storeID + " (no entry to cancel): ");
                        input = in.readLine();
                        if (input.isEmpty()) { orderProduct = ""; valid = true; }
                        else {
                            orderProduct = input;
                            valid = verifyInput(esql, "Product", "productName", input) && checkStore(esql, storeID, input);
                        }
                    } while (!valid);

                    if (!orderProduct.isEmpty() && valid) {
                        List<List<String>> productInfo = esql.executeQueryAndReturnResult(String.format("SELECT numberOfUnits, pricePerUnit FROM Product WHERE productName = '%s' AND storeID = '%d'", orderProduct, storeID));
                        availableUnits = Integer.parseInt(productInfo.get(0).get(0));
                        
                        do {
                            System.out.println("\nStore " + storeID + " has " + availableUnits + " units of " + orderProduct + " available for order at $" + productInfo.get(0).get(1) + " per unit.");
                            System.out.print("Enter the number of units of " + orderProduct + " you want to order (no entry to cancel): ");
                            input = in.readLine();

                            if (input.isEmpty()) { orderCount = -1; valid = true; }
                            else {
                                orderCount = parseIntCheck(input);
                                if (orderCount != -1) {
                                    if (orderCount <= availableUnits) { valid = true; }
                                    else { System.out.println("You cannot order more units than the store has available"); valid = false; }
                                }
                                else { valid = false; }
                            }
                        } while (!valid);

                        if (orderCount != -1) {
                            esql.executeUpdate(String.format("INSERT INTO Orders VALUES (DEFAULT, '%d', '%d', '%s', '%d', localtimestamp)", authorisedUser, storeID, orderProduct, orderCount));
                            esql.executeUpdate(String.format("UPDATE Product SET numberOfUnits = numberOfUnits - '%d' WHERE storeID = '%d' AND productName = '%s'", orderCount, storeID, orderProduct));
                            System.out.println("Order placed for " + orderCount + " units of " + orderProduct + " from store " + storeID + ".");
                        }
                    }
                }
            }
            else {
                System.out.print("No stores found within 30 miles of your location.");
                System.out.println();
            }
        }catch(Exception e){
            System.err.print(e.getMessage());
        }
    }

   public static void viewRecentOrders(Amazon esql, int userID) {
        try {
            esql.executeQueryAndPrintResult(String.format("SELECT storeID, productName, unitsOrdered, orderTime FROM Orders WHERE customerID = '%d' ORDER BY orderTime DESC LIMIT 5", userID));
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
   
   public static void updateProduct(Amazon esql, int managerID, int admin) {
        try{
            String input = "";
            String productQuery = "";
            boolean valid = false;
            int storeID = -1;
            String nameUpdate = "";
            int numUnitsUpdate;
            float priceUpdate;
            String productName = "";
            boolean updatingStores = false;
            boolean updatingProducts = false;
            
            do {
                numUnitsUpdate = 0;
                priceUpdate = 0;

                if (admin == 0) { esql.executeQueryAndPrintResult(String.format("SELECT storeID, latitude, longitude FROM Store WHERE managerID = '%d'", managerID)); }
                else { esql.executeQueryAndPrintResult(String.format("SELECT storeID, latitude, longitude FROM Store")); }

                do { // Collect Store ID, verify
                    System.out.print("\tEnter the ID of the store you are updating a product at: ");
                    input = in.readLine();
                    storeID = parseIntCheck(input);
                    if (storeID != -1) { 
                        if (admin == 0) { valid = verifyInput(esql, "Store", "storeID", input) && verifyManager(esql, managerID, storeID); }
                        else { valid = verifyInput(esql, "Store", "storeID", input); }
                    }
                    else { valid = false; }
                } while (!valid);

                do { // Collect product name, ask if updating more products
                    esql.executeQueryAndPrintResult(String.format("SELECT productName, numberOfUnits, pricePerUnit FROM Product WHERE storeID = '%d'", storeID));
            
                    do { // Collect product name, verify
                        System.out.print("\tEnter the name of the product you are updating: ");
                        productName = in.readLine();
                        valid = verifyInput(esql, "Product", "productName", productName) && checkStore(esql, storeID, productName);
                    } while (!valid);
             
                    if (admin == 1) { // Name change is admin only
                        do { // Request updated product name
                            System.out.print("\tUpdate product name? Provide updated name (no entry to skip): ");
                            input = in.readLine();
                            if (input.isEmpty()) { valid = true; nameUpdate = ""; }
                            else {
                                if (checkStore(esql, storeID, input)) { System.out.println("Product named " + input + " already exists at store " + storeID + "."); valid = false; nameUpdate = ""; }
                                else if (input.length() > 30) { System.out.println("Product name must be 30 characters or less."); valid = false; nameUpdate = ""; }
                                else { valid = true; nameUpdate = input; }
                            }
                        } while (!valid);
                    }

                    do { // Request updated number of units
                        System.out.print("\tUpdate number of units? Provide updated value (no entry to skip): ");
                        input = in.readLine();
                        if (input.isEmpty()) { valid = true; numUnitsUpdate = -1; }
                        else {
                            numUnitsUpdate = parseIntCheck(input);
                            if (numUnitsUpdate != -1) { valid = true; }
                            else { valid = false; }
                        }
                    } while (!valid);

                    do { // Request updated price
                        System.out.print("\tUpdate price per unit? Provide updated price (no entry to skip): ");
                        input = in.readLine();
                        if (input.isEmpty()) { valid = true; priceUpdate = -1; }
                        else {
                            priceUpdate = parseFloatCheck(input);
                            if (priceUpdate != -1) { valid = true; }
                            else { valid = false; }
                        }
                    } while (!valid);

                    if (admin == 1) {
                        if (!nameUpdate.isEmpty()) {
                            productQuery = String.format("UPDATE Product SET productName = '%s' WHERE storeID = '%d' AND productName = '%s'", nameUpdate, storeID, productName);
                            esql.executeUpdate(productQuery);
                            productName = nameUpdate;
                            esql.executeUpdate(String.format("INSERT INTO ProductUpdates VALUES (DEFAULT, '%d', '%d', '%s', localtimestamp)", managerID, storeID, productName));
                        }
                    }

                    // If at least one is not -1, actual update values have been provided, construct and execute update queries
                    if (numUnitsUpdate != -1) {
                        if (priceUpdate != -1) { // Update both
                            productQuery = String.format("UPDATE Product SET (numberOfUnits, pricePerUnit) = ('%d', '%f') WHERE storeID = '%d' AND productName = '%s'", numUnitsUpdate, priceUpdate, storeID, productName);
                        }
                        else { // Update only number of units
                            productQuery = String.format("UPDATE Product SET numberOfUnits = '%d' WHERE storeID = '%d' AND productName = '%s'", numUnitsUpdate, storeID, productName);
                        }

                        esql.executeUpdate(productQuery);
                        esql.executeUpdate(String.format("INSERT INTO ProductUpdates VALUES (DEFAULT, '%d', '%d', '%s', localtimestamp)", managerID, storeID, productName));
                    }
                    else if (priceUpdate != -1) { // Update only price
                        productQuery = String.format("UPDATE Product SET pricePerUnit = '%f' WHERE storeID = '%d' AND productName = '%s'", priceUpdate, storeID, productName); 
                        esql.executeUpdate(productQuery);
                        esql.executeUpdate(String.format("INSERT INTO ProductUpdates VALUES (DEFAULT, '%d', '%d', '%s', localtimestamp)", managerID, storeID, productName));
                    }

                    System.out.println();

                    do { // Ask if updating more products
                        System.out.print("\tDo you want to update another product at store " + storeID + "? [y/N]: ");
                        input = in.readLine().trim();
                        if (input.isEmpty()) { valid = true; updatingProducts = false; }
                        else {
                            switch (input) {
                                case "y":
                                case "Y": valid = true; updatingProducts = true; break;
                                case "n":
                                case "N": valid = true; updatingProducts = false; break;
                                default: valid = false;
                            }
                        }

                    } while (!valid);
                } while (updatingProducts);

                System.out.println();
        
                do { // Ask if updating products for a different store
                    System.out.print("\tDo you want to update products for another store? [y/N]: ");
                    input = in.readLine();
                    if (input.isEmpty()) { valid = true; updatingStores = false; }
                    else {
                        switch (input) {
                            case "y":
                            case "Y": valid = true; updatingStores = true; break;
                            case "n":
                            case "N": valid = true; updatingStores = false; break;
                            default: valid = false;
                        }
                    }
                } while (!valid);
            } while (updatingStores);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
   
   public static void viewRecentUpdates(Amazon esql, int managerID) {
        try{
            String query = String.format("SELECT * FROM ProductUpdates WHERE storeID in (SELECT storeID FROM Store WHERE managerID = '%d') ORDER BY updatedOn DESC LIMIT 5", managerID);        
            esql.executeQueryAndPrintResult(query);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
   
   public static void viewPopularProducts(Amazon esql, int managerID) {
        try{
            String query = String.format("SELECT productName, COUNT(*) as order_count FROM Orders WHERE storeID IN (SELECT storeID FROM Store WHERE managerID = '%d') GROUP BY productName ORDER BY order_count DESC LIMIT 5", managerID);
            esql.executeQueryAndPrintResult(query);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
   
   public static void viewPopularCustomers(Amazon esql, int managerID) {
        try{
            String query = String.format("SELECT o.customerID, u.name, u.latitude, u.longitude, COUNT(*) as order_count FROM Orders o JOIN Users u ON o.customerID = u.userID WHERE storeID IN (SELECT storeID FROM Store WHERE managerID = '%d') GROUP BY o.customerID, u.name, u.latitude, u.longitude ORDER BY order_count DESC LIMIT 5", managerID);
            esql.executeQueryAndPrintResult(query);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

   public static void placeProductSupplyRequests(Amazon esql, int managerID) {
        try{
            String input = "";
            boolean valid = false;
            int storeID = -1;
            String productName = "";
            int unitsRequested = -1;
            int warehouseID = -1;

            do { // Collect storeID, verify 
                System.out.print("\tEnter Store ID: ");
                input = in.readLine();
                storeID = parseIntCheck(input);
                if (storeID != -1) { valid = verifyInput(esql, "Store", "storeID", input); }
                else { valid = false; }
            } while (!valid);

            do { // Collect productName, verify
                System.out.print("\tEnter product name: ");
                productName = in.readLine();
                if (productName.length() > 30) {
                    System.out.println("Invalid input. Entered product name is too long, must be less than 30 characters.");
                    valid = false;
                }
                else { valid = verifyInput(esql, "Product", "productName", productName); }
            } while (!valid);

            do { // Collect requested number of units, verify
                System.out.print("\tEnter requested number of units of " + productName + ": ");
                input = in.readLine();
                unitsRequested = parseIntCheck(input);
                if (unitsRequested != -1) {
                    if (unitsRequested == 0) {
                        System.out.println("Invalid input. You must request at least 1 unit.");
                        valid = false;
                    } else { valid = true; }
                } else { valid = false; }
            } while (!valid);

            do { // Collect warehouseID, verify
                System.out.print("\tEnter the ID of the warehouse to request from: ");
                input = in.readLine();
                warehouseID = parseIntCheck(input);
                if (warehouseID != -1) { valid = verifyInput(esql, "Warehouse", "WarehouseID", input); }
                else { valid = false; }
            } while (!valid);

            String insertQuery = String.format("INSERT INTO ProductSupplyRequests (managerID, warehouseID, storeID, productName, unitsRequested) VALUES ('%d', '%d', '%d', '%s', '%d')", managerID, warehouseID, storeID, productName, unitsRequested);
            String productQuery = "";

            if (checkStore(esql, storeID, productName)) { // Store has productName, update amount
                productQuery = String.format("UPDATE Product SET numberOfUnits = numberOfUnits + %d WHERE storeID = '%d' AND productName = '%s'", unitsRequested, storeID, productName);
            }
            else { // Store does not have productName, insert into Products
                float nearestPrice = getNearestPrice(esql, storeID, productName);
                if (nearestPrice != -1) {
                    productQuery = String.format("INSERT INTO Product (storeID, productName, numberOfUnits, pricePerUnit) VALUES ('%d', '%s', '%d', '%f')", storeID, productName, unitsRequested, nearestPrice);
                } else { productQuery = ""; }
            }
            
            esql.executeUpdate(insertQuery);
            esql.executeUpdate(productQuery);
            
            System.out.println("Order for " + unitsRequested + " unit(s) of " + productName + " placed for Store " + storeID + " from Warehouse " + warehouseID + ".");
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static void viewStoreOrders(Amazon esql, int managerID) {
        try{
            boolean viewingOrders = true;
            boolean valid = false;
            String input = "";
            int storeID = -1;

            do {
                
                do { // Collect Store ID, verify
                    System.out.print("\tEnter the ID of the store to view orders from: ");
                    input = in.readLine();
                    storeID = parseIntCheck(input);
                    if (storeID != -1) { valid = verifyInput(esql, "Store", "storeID", input) && verifyManager(esql, managerID, storeID); }
                    else { valid = false; }
                } while (!valid);

                String query = String.format("SELECT * FROM Orders WHERE storeID = '%s'", storeID);
                esql.executeQueryAndPrintResult(query);

                do { // Ask if viewing orders for a different store
                    System.out.print("\tDo you want to view orders from another store? [y/N]: ");
                    input = in.readLine();
                    if (input.isEmpty()) { valid = true; viewingOrders = false; }
                    else {
                        switch (input) {
                            case "y":
                            case "Y": valid = true; viewingOrders = true; break;
                            case "n":
                            case "N": valid = true; viewingOrders = false; break;
                            default: valid = false;
                        }
                    }
                } while (!valid);
 
            } while (viewingOrders);

        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static void adminUpdateUser(Amazon esql) {
        try{
            boolean editingUsers = true;
            boolean editingField = true;
            boolean valid = false;
            int editChoice = -1;
            String input = "";
            String secondInput = "";
            int targetID = -1;

            String updateQuery = "";

            do {
                    
                esql.executeQueryAndPrintResult("SELECT * FROM Users");

                do { // Choose ID
                    System.out.print("\tInput the user ID of the user you are editing (no entry to cancel): ");
                    input = in.readLine();
                    
                    if (!input.isEmpty()) {
                        targetID = parseIntCheck(input);
                        if (targetID != -1) { valid = verifyInput(esql, "Users", "userID", input); }
                    }
                    else { valid = true; targetID = -1; }

                } while (!valid);

                if (targetID != -1) { // ID chosen, not empty

                    do {

                        System.out.println("1. Name");
                        System.out.println("2. Password");
                        System.out.println("3. Latitude / Longitude");
                        System.out.println("4. User Type");
                        System.out.println("5. Done");

                        switch (readChoice()) {
                            case 1:
                                
                                do {
                                    System.out.print("\tEnter the updated name with a max of 50 characters (no entry to cancel): ");
                                    input = in.readLine();
                                    
                                    if (input.isEmpty()) { break; }

                                } while (!nameUnique(esql, input) || input.length() > 50);
                                
                                updateQuery = String.format("UPDATE Users SET name = '%s' WHERE userID = '%d'", input, targetID);
                                esql.executeUpdate(updateQuery);

                                break;
                            case 2:

                                do {
                                    System.out.print("\tEnter the updated password with between 3 and 11 characters (no entry to cancel): ");
                                    input = in.readLine();

                                    if (input.isEmpty()) { break; }

                                } while (input.length() < 3 || input.length() > 11);

                                updateQuery = String.format("UPDATE Users SET password = '%s' WHERE userID = '%d'", input, targetID);
                                esql.executeUpdate(updateQuery);

                                break;
                            case 3:
                                
                                do { 
                                    System.out.print("\tEnter the updated latitude coordinate, between -90 and 90 with up to six digits after the decimal (no entry to cancel): ");
                                    input = in.readLine();

                                    if (input.isEmpty()) { break; }

                                    System.out.print("\tEnter the updated latitude coordinate, between -180 and 180 with up to six digits after the decimal (no entry to cancel): ");
                                    secondInput = in.readLine();
                                    
                                    if (secondInput.isEmpty()) { break; }

                                } while (!validateLocationInput(input, true) || !validateLocationInput(input, false));

                                updateQuery = String.format("UPDATE Users SET latitude = '%s', longitude = '%s' WHERE userID = '%d'", input, secondInput, targetID);
                                esql.executeUpdate(updateQuery);

                                break;
                            case 4:
                                
                                do {
                                    System.out.println("\n1. Customer");
                                    System.out.println("2. Manager");
                                    System.out.println("3. Admin");
                                    System.out.print("\tEnter the updated user type (no entry to cancel): ");
                                    input = in.readLine();

                                    if (!input.isEmpty()) {
                                        switch (input) {
                                            case "1":
                                                input = "customer";
                                                valid = true;
                                                break;
                                            case "2":
                                                input = "manager";
                                                valid = true;
                                                break;
                                            case "3":
                                                input = "admin";
                                                valid = true;
                                                break;
                                            default:
                                                System.out.println("Unrecognized choice.");
                                                valid = false;
                                                break;
                                        }
                                    }
                                    else { input = "cancel"; valid = true; }
                                } while (!valid); 

                                if (!input.equals("cancel")) {
                                    updateQuery = String.format("UPDATE Users SET type = '%s' WHERE userID = '%d'", input, targetID);
                                    esql.executeUpdate(updateQuery);
                                }

                                break;
                            case 5:
                                editingField = false;
                                break;
                            default: System.out.println("Unrecognized choice."); break;
                        }

                        do {
                            System.out.print("Would you like to update another field? [y/N]: ");
                            input = in.readLine().trim();
                            if (input.isEmpty()) { valid = true; editingField = false; }
                            else {
                                switch (input) {
                                    case "y":
                                    case "Y": valid = true; editingField = true; break;
                                    case "n":
                                    case "N": valid = true; editingField = false; break;
                                    default: valid = false; break;
                                }
                            }
                        } while (!valid);

                    } while (editingField);
                }

                do {
                    System.out.print("\tWould you like to update another user's information? [y/N]: ");
                    input = in.readLine().trim();
                    if (input.isEmpty()) { valid = true; editingUsers = false; }
                    else {
                        switch (input) {
                            case "y":
                            case "Y": valid = true; editingUsers = true; break;
                            case "n":
                            case "N": valid = true; editingUsers = false; break;
                            default: valid = false; break;
                        }
                    }
                } while (!valid);

            } while (editingUsers);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

}//end Amazon

