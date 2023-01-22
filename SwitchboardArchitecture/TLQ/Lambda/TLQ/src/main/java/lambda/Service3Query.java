package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.GetObjectRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author Divya Jacob
 */
public class Service3Query implements RequestHandler<Request, List<List<QueryResult>>> {
    
    public List<List<QueryResult>> handleRequest(Request request, Context context) {

        // Create logger
        LambdaLogger logger = context.getLogger();
       
        
        //****************START FUNCTION IMPLEMENTATION*************************
        Response r = new Response();
        
        String bucketname = request.getBucketname();
        String filename = request.getFilename();
        
        setCurrentDirectory("/tmp");
        
        File file = new File("/tmp/sales.db");
        
        if(!file.exists() || file.isDirectory()){
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
            //get db file using source bucket and srcKey name and save to /tmp
            //get object file using source bucket and srcKey name
            s3Client.getObject(new GetObjectRequest(bucketname, filename),file);
        }

        try
        {
            String filter = request.getFilter();
            String groupBy = request.getGroupby();
            String aggregation = request.getAggregation();
            
            StringBuilder sqlQuery = new StringBuilder();
            sqlQuery.append("select ");
            if(aggregation != null && aggregation.length()>0) {
                sqlQuery.append(aggregation);
            } else {
                sqlQuery.append(" *");
            }
            
            sqlQuery.append(" from sales_data");
            if(filter != null && filter.length() > 0){
                sqlQuery.append(" where ");
                sqlQuery.append(filter); 
            }
            
            if(groupBy != null && groupBy.length() > 0){
                sqlQuery.append(" group by ");
                sqlQuery.append(groupBy); 
            }

            System.out.println(sqlQuery);
            logger.log("Query:" + sqlQuery +"\n");
            
            
            try { Class.forName("org.sqlite.JDBC"); } catch(Exception e) {e.printStackTrace();};
            // Connection string for a file-based SQlite DB
            Connection con = DriverManager.getConnection("jdbc:sqlite:/tmp/sales.db");

        
            PreparedStatement ps = con.prepareStatement(sqlQuery.toString());
            ResultSet rs = ps.executeQuery();

            List<List<QueryResult>> results = new ArrayList<>();
            String[] aggregations = aggregation.split(",");
            while (rs.next()) {
                List<QueryResult> result = new ArrayList<>();
                for (int i=0; i<aggregations.length; i++) {
                    if(rs.getString(i+1) != null){
                        String query = aggregations[i];
                        logger.log("result" + rs.getString(i+1) +"\n");
                        try {
                            double value = Double.parseDouble(rs.getString(i+1));
                            QueryResult queryResult = new QueryResult(query, value);
                            result.add(queryResult);
                        } catch(Exception e){
                            
                        }
                    }
                }
                results.add(result);
            } 
            rs.close();
            con.close();  

            r.setResults(results);
        }
        catch (SQLException sqle)
        {
            logger.log("DB ERROR:" + sqle.toString());
            sqle.printStackTrace();
        }

        
        //****************END FUNCTION IMPLEMENTATION***************************
        
        return r.getResults();
    }
    
    public static boolean setCurrentDirectory(String directory_name)
    {
        boolean result = false;  // Boolean indicating whether directory was set
        File    directory;       // Desired current working directory

        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs())
        {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }

        return result;
    }
    
}
