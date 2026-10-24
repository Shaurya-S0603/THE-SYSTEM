package app.thesystem.mobile;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
public class StateSchemaTest {
 private JSONObject fixture()throws Exception{return new JSONObject("{\"version\":1,\"settings\":{\"modules\":[\"planner\",\"budget\"],\"alias\":\"Nova-07\",\"shield\":\"Aegis\",\"currency\":\"SGD\",\"monthlyLimit\":10000,\"limits\":{\"Food\":5000,\"Travel\":0,\"Learning\":0,\"Personal\":0,\"Other\":0},\"reminder\":\"off\"},\"quests\":[],\"events\":[],\"expenses\":[],\"rewards\":[],\"nextId\":1}");}
 @Test public void acceptsStructuredState()throws Exception{StateSchema.validate(fixture());}
 @Test public void rejectsRealNameField()throws Exception{JSONObject s=fixture();s.getJSONObject("settings").put("name","fixture");try{StateSchema.validate(s);fail();}catch(IllegalArgumentException e){assertEquals(StateSchema.METADATA,e.getMessage());}}
 @Test public void rejectsCustomAlias()throws Exception{JSONObject s=fixture();s.getJSONObject("settings").put("alias","custom");try{StateSchema.validate(s);fail();}catch(IllegalArgumentException expected){}}
 @Test public void rejectsNestedExpenseMetadata()throws Exception{JSONObject s=fixture();s.put("nextId",2);s.getJSONArray("expenses").put(new JSONObject("{\"id\":1,\"category\":\"Food\",\"amount\":10,\"day\":\"2026-10-24\",\"merchant\":\"fixture\"}"));try{StateSchema.validate(s);fail();}catch(IllegalArgumentException expected){}}
 @Test public void rejectsFractionalMoney()throws Exception{JSONObject s=fixture();s.getJSONObject("settings").put("monthlyLimit",10.5);try{StateSchema.validate(s);fail();}catch(IllegalArgumentException expected){}}
 @Test public void rejectsInvalidDates()throws Exception{try{StateSchema.day("2026-02-30");fail();}catch(IllegalArgumentException expected){}}
 @Test public void rejectsOverallocatedBudget()throws Exception{JSONObject s=fixture();s.getJSONObject("settings").put("monthlyLimit",10);try{StateSchema.validate(s);fail();}catch(IllegalArgumentException expected){}}
 @Test public void rejectsDuplicateModules()throws Exception{JSONObject s=fixture();s.getJSONObject("settings").put("modules",new JSONArray("[\"planner\",\"planner\"]"));try{StateSchema.validate(s);fail();}catch(IllegalArgumentException expected){}}
}
