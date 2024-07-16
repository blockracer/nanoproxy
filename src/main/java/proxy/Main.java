package proxy;


import static spark.Spark.*;


//gson
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

//unirest
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.*;


import spark.Filter;
import spark.Request;
import spark.Response;




public class Main {

    private static final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    private static final Map<String, Integer> workRequestCounts = new ConcurrentHashMap<>();

    private static final Map<String, Long> nextResetMap = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {

	scheduler.scheduleAtFixedRate(() -> {
		//check if current time is greater than next reset time.
		//cycle through the map	
		for (Map.Entry<String, Long> entry : nextResetMap.entrySet()) {
            		String ip = entry.getKey();
            		long nextResetTime = entry.getValue();
        		long currentTime = System.currentTimeMillis();
			if(currentTime > nextResetTime) {
				//remove from map
				nextResetMap.remove(ip);
			}
        }
				
	}, 0, 5, TimeUnit.SECONDS);


	//This is the port of yur proxy, change to the port you want.
        port(1234);

        enableCORS("*", "*", "*");
	//Add your url path here leave it as / if its for your root domain.
        post("/", (req, res) -> {
            res.type("application/json");

            String ip = req.headers("CF-Connecting-IP");

            String jsonPayload = req.body();
            System.out.println(jsonPayload);
            JsonObject payloadObj = JsonParser.parseString(jsonPayload).getAsJsonObject();
            String action = payloadObj.get("action").getAsString();
		//Add all your blacklisted requests here.
            if (action.equals("send") || action.equals("node_id") || action.equals("node_id_delete") || action.equals("stop")) {
                return "This action is not allowed";
            }

            // Default limit for actions other than "work_generate Edit for your requiements"
            int limit = 30;

            if (action.equals("work_generate")) {
                // Increase limit for "work_generate" action
                limit = ip.equals("localhost") ? 999 : 10; // Allow 999 requests for the specific IP localhost as the place holder for all other ip's change the 10 value to what you want to limit work generate requests.
		boolean iswork = true;
								//
            	if (!allowRequest(ip, limit, iswork)) {
			return "Rate limit exceeded";
		}
            }
	    else {
		boolean iswork = false;

            	if (!allowRequest(ip, limit, iswork)) {
                	return "Rate limit exceeded";
            	}
	    }
		
            String nanoNodeRpcAddress = "[::1]"; // Your rpc address, it could run on ipv4 or ipv6 address.
            int rpcPort = 7076; // Replace with your actual RPC port

            String url = "http://" + nanoNodeRpcAddress + ":" + rpcPort;

            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.post(url)
                        .header("Content-Type", "application/json")
                        .body(jsonPayload)
                        .asJson();

                System.out.println("Response Code: " + jsonResponse.getStatus());

                if (jsonResponse.getStatus() == 200) {
                    String jsonBody = jsonResponse.getBody().toString();
                    JsonObject jsonObject = JsonParser.parseString(jsonBody).getAsJsonObject();
                    return jsonObject;
                } else {
                    return "Error: " + jsonResponse.getStatusText();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Something failed";
        });
    }

    public static void enableCORS(final String origin, final String methods, final String headers) {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", origin);
            res.header("Access-Control-Request-Method", methods);
            res.header("Access-Control-Allow-Headers", headers);
            res.type("application/json");
        });
    }
 	// Limit the number of requests per minute
    public static synchronized boolean allowRequest(String ip, int limit, boolean iswork) {
        	long currentTime = System.currentTimeMillis();


		long nextReset = 0L;

		if (!nextResetMap.containsKey(ip)) { 
			// Edit the 60000 (1 min) to set the time interval for the request limits.
			nextReset = currentTime + 60000;
			nextResetMap.put(ip, nextReset);

			workRequestCounts.put(ip, 0);
			requestCounts.put(ip, 0);

			return true;
		}
		else {
			//check current request counts against the limit
			if(iswork == false) {

        			int requestCount = requestCounts.get(ip);


				if(requestCount > limit) {
					return false;
				}
				else {
					int newCount = requestCount +1;
					requestCounts.put(ip, newCount);
					return true;
				}
			}
			else {
				
				int workRequestCount = workRequestCounts.get(ip);

				if(workRequestCount > limit) {
					return false;
				}
				else {
					int newCount = workRequestCount +1;
					workRequestCounts.put(ip, newCount);

					return true;
				}
			}
		}
	}
}

   
