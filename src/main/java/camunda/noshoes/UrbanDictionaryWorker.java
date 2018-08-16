package camunda.noshoes;

import java.util.HashMap;

import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.client.ExternalTaskClient;

import com.jayway.jsonpath.JsonPath;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class UrbanDictionaryWorker {
  private final static Logger LOGGER = Logger.getLogger(UrbanDictionaryWorker.class.getName());

  public static void main(String[] args) {
	  
    ExternalTaskClient client = ExternalTaskClient.create()
        .baseUrl("http://localhost:8080/engine-rest")
        .build();

    // subscribe to an external task topic as specified in the process
    client.subscribe("Urban")
        .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
        .handler((externalTask, externalTaskService) -> {
          // Put your business logic here
        	
	          // Get a process variable
	        	String city = (String) externalTask.getVariable("city");
	        	String name = (String) externalTask.getVariable("name");
	          //LOGGER.info("Charging credit card with an amount of '" + amount + "'€ for the item '" + item + "'...");
	          
	          HttpResponse<String> response = null;
	          try {
	        	  
	        	  response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term="+name)
						  .header("X-Mashape-Key", "OAkOqCcGiOmshuS6iijbRbY4vrsSp1GezTIjsnUX0eEn2EuNMw")
						  .header("Accept", "text/plain")
						  .asString();
	        	  
	        	  System.out.println(response.getBody().toString());
				
				} catch (UnirestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	          
		         String def = "You're just undefinable";
		         
		         try { 
		        	 def = JsonPath.read(response.getBody(), "$.list[0].definition");
		         }
		         catch(Exception e) {
		        	 System.out.println("No Def Found on urban");
		         }
		         
		         System.out.println(def);
		          
		          Map<String, Object> variables = new HashMap<>();
		  
		          variables.put("UrbanDef", def);
		
		          // Complete the task
		          externalTaskService.complete(externalTask, variables);
          
        })
        .open();
        
  }
}
