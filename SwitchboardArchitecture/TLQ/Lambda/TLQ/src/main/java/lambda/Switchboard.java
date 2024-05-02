package lambda;

import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import saaf.Inspector;
import saaf.Response;

public class Switchboard implements RequestHandler<Request, HashMap<String, Object>>{
    public HashMap<String, Object> handleRequest(Request request, Context context){
    	Inspector inspector = new Inspector();
       
       Object s =new  String();
       Response response = new Response();
       
        String servicename=request.getServicename();
        if("service1".equals(servicename)){
            Service1 pc = new Service1();
            s = pc.handleRequest(request, context);
            response.setValue(s.toString());

        }
        else if("service2".equals(servicename)){
          Service2DataLoad sdl = new Service2DataLoad();
          s = sdl.handleRequest(request, context);
          response.setValue(s.toString());
        }        
        else if("service3".equals(servicename)){
           Service3Query serv3 = new Service3Query ();
           List<List<QueryResult>> qr =  serv3.handleRequest(request, context);
          
           for(List<QueryResult> q :qr )
           {
           if(response.getValue()!=null)
          { response.setValue(response .getValue() + "{" + '\n'  +  "query "+":" + q.get(0).getQuery() + '\n'+ "result" + ":"  +q.get(0).getValue() + '\n'+"}" + "{" + '\n'  +  "query "+":" + q.get(1).getQuery() + '\n'+ "result" + ":"  +q.get(1).getValue() + '\n'+"}" );
        }
        else{
             response.setValue("{" + '\n'  +  "query "+":" + q.get(0).getQuery() + '\n'+ "result" + ":"  +q.get(0).getValue() + '\n'+"}" + "{" + '\n'  +  "query "+":" + q.get(1).getQuery() + '\n'+ "result" + ":"  +q.get(1).getValue() + '\n'+"}" );

        
           
          
           
        }
    }
    }
        
        
        
        inspector.consumeResponse(response);
        return inspector.finish();
       
        
        
  
    }

    
}
