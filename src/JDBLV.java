import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class JDBLV {

    public  final String DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
    public  final String CONN_STRING = "jdbc:derby:C:\\AreometerVerifier\\appdata\\db;user=remoteclient;password=remoteclient;"; //- протокол//адрес:порт/параметры
    private  HashMap<String ,ArrayList<String>> resultMap = new HashMap<>();

    public JDBLV() {
    }

    public String creation() {

        Enumeration<Driver> drivers = DriverManager.getDrivers(); //системный класс(создавать объекты не нужно)
        while(drivers.hasMoreElements()){
            System.out.println(drivers.nextElement());
        }

        try {
            Class.forName(DRIVER_NAME); //принудительная загрузка байткода класса в память
        } catch (ClassNotFoundException ex) {
            System.out.println("Cannot load driver for Derby!");
            return "Cannot load driver for Derby!";
        }

        //подключение к серверу БД
        Connection conn = null; //для работы с БД (java.sql) //con будет базой
        try {
            conn = DriverManager.getConnection(CONN_STRING);
        } catch (SQLException ex) {
            System.out.println("Cannot open connection to DB! " + ex.getMessage());
            return "Cannot open connection to DB! " + ex.getMessage();
        }

        try {
            Statement st = conn.createStatement(); //запрос
            ArrayList<BigDecimal> list = new ArrayList<>();
            ResultSet rs = st.executeQuery("SELECT ROOT.EXPERIMENT.ID, CREATIONDATETIME FROM ROOT.EXPERIMENT\n" +
                    "WHERE\n" +
                    "\n" +
                    "CREATIONDATETIME >= '2020-11-27 00:00:00'\n" +
                    "\n" +
                    "AND CREATIONDATETIME <= '2020-11-27 23:59:59'\n" +
                    "\n" +
                    "AND APPLICATION_ID IS NOT NULL");

            while (rs.next()){
//                System.out.println(rs.getString("ID")+" "+rs.getString("CREATIONDATETIME"));
                list.add(rs.getBigDecimal("ID"));
            }
            BigDecimal lastId = Collections.max(list);
//            System.out.println(lastId);

            rs = st.executeQuery("SELECT\n" +
                    "\n" +
                    "ROOT.WEIGHING.ID, AIRDENSITY, AIRHUMIDITY, AIRPRESSSURE, AIRTEMPERATURE,\n" +
                    "\n" +
                    "AREOMETERCALIBRATIONMARK, AREOMETERDIAMETER, AREOMETERWEIGHT, CORRECTIONA,\n" +
                    "\n" +
                    "CORRECTIONB, CORRECTIONC, LIQUIDDENSITY, LIQUIDREALDENSITY, LIQUIDTEMPERATURE,\n" +
                    "\n" +
                    "MENISCUSA, MENISCUSB, MENISCUSC, MENISCUSREAL, EXPERIMENT_ID,          \n" +
                    "\n" +
                    "(SELECT NAME FROM ROOT.LIQUID WHERE ID=ROOT.WEIGHING.LIQUIDA_ID) AS LIQUIDA_NAME,\n" +
                    "\n" +
                    "(SELECT NAME FROM ROOT.LIQUID WHERE ID=ROOT.WEIGHING.LIQUIDB_ID) AS LIQUIDB_NAME,\n" +
                    "\n" +
                    "(SELECT NAME FROM ROOT.LIQUID WHERE ID=ROOT.WEIGHING.LIQUIDC_ID) AS LIQUIDC_NAME\n" +
                    "\n" +
                    "FROM ROOT.WEIGHING\n" +
                    "\n" +
                    "WHERE EXPERIMENT_ID = "+lastId.toString());

            ResultSetMetaData rsmd = rs.getMetaData();
            String name;
            while(rs.next()){
                for (int i = 1; i < rsmd.getColumnCount(); i++) {
                    name = rsmd.getColumnName(i);
                    if (!resultMap.containsKey(name)) {
                        resultMap.put(name, new ArrayList<>());
                    }
                    resultMap.get(name).add(rs.getString(name));
                }
            }
      
        } catch(SQLException ex) {
            System.out.println(ex.getMessage());
        }
        if(resultMap.isEmpty()) {
            return "map is empty";
        }
        else return "map ok";
    }

    public  String[][] getAllResults(){
        String[][] res2DMass = new String[resultMap.size()][4];
        int i = 0;
        for (Map.Entry<String, ArrayList<String>> entry: resultMap.entrySet()) {
            res2DMass[i][0] = entry.getKey();
            int j = 1;
            for (String value:entry.getValue()) {
                res2DMass[i][j] = value;
                j++;
            }
            i++;
        }
//        for (int j = 0; j < res2DMass.length; j++) {
//            for (int k = 0; k < 4; k++) {
//                System.out.print(res2DMass[j][k]+" ");
//            }
//            System.out.println("\n");
//        }
        return res2DMass;
    }

    public  String[] getValue(String key){
        String[] value = new String[3];
        int i = 0;
        for (String str:resultMap.get(key)) {
            value[i] = str;
            i++;
        }
        for (int j = 0; j < value.length; j++) {
            System.out.println(value[j]);
        }
        return value;
    }
}
