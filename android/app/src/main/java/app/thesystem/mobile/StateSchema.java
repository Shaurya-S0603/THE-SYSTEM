package app.thesystem.mobile;

import java.util.*;
import org.json.*;

/** Reject unknown fields and all unrestricted strings before encrypted persistence. */
final class StateSchema {
    static final String METADATA="[ERROR: System cannot process real-world metadata.]";
    private static void reject(){throw new IllegalArgumentException(METADATA);}
    static void exact(JSONObject o,String... allowed){Set<String> set=new HashSet<>(Arrays.asList(allowed));if(o.length()!=set.size())reject();Iterator<String> it=o.keys();while(it.hasNext())if(!set.contains(it.next()))reject();}
    static void choice(Object value,String... allowed){if(!(value instanceof String)||!Arrays.asList(allowed).contains(value))reject();}
    static void number(Object value,long min,long max){if(!(value instanceof Number))reject();double n=((Number)value).doubleValue();if(!Double.isFinite(n)||n!=Math.floor(n)||n<min||n>max)reject();}
    static void day(Object value){if(!(value instanceof String)||!((String)value).matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))reject();try{java.time.LocalDate.parse((String)value);}catch(Exception e){reject();}}
    static void validate(JSONObject s) throws JSONException {
        exact(s,"version","settings","quests","events","expenses","rewards","nextId");number(s.get("version"),1,1);number(s.get("nextId"),1,1000000000);
        if(!s.isNull("settings")){
            JSONObject c=s.getJSONObject("settings");exact(c,"modules","alias","shield","currency","monthlyLimit","limits","reminder");
            JSONArray modules=c.getJSONArray("modules");if(modules.length()<1||modules.length()>5)reject();Set<String> seen=new HashSet<>();for(int i=0;i<modules.length();i++){choice(modules.get(i),"planner","timetable","budget","levelup","focus");if(!seen.add(modules.getString(i)))reject();}
            choice(c.get("alias"),"Nova-07","Echo-12","Orion-03","Astra-09");choice(c.get("shield"),"Aegis","Prism","Orbit");choice(c.get("currency"),"SGD","USD","EUR","GBP","INR");choice(c.get("reminder"),"off","18:00");number(c.get("monthlyLimit"),0,100000000);
            JSONObject limits=c.getJSONObject("limits");exact(limits,"Food","Travel","Learning","Personal","Other");long sum=0;for(String k:Arrays.asList("Food","Travel","Learning","Personal","Other")){number(limits.get(k),0,100000000);sum+=limits.getLong(k);}if(sum>c.getLong("monthlyLimit"))reject();
        }
        Set<Long> ids=new HashSet<>();
        for(String name:Arrays.asList("quests","events","expenses","rewards")){
            JSONArray a=s.getJSONArray(name);if(a.length()>2000)reject();
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i);number(o.get("id"),1,s.getLong("nextId")-1);if(!ids.add(o.getLong("id")))reject();
                switch(name){
                case "quests":exact(o,"id","kind","effort","day","done");choice(o.get("kind"),"Study","Practice","Read","Build","Exercise","Organize");choice(o.get("effort"),"small","medium","large");day(o.get("day"));if(!(o.get("done") instanceof Boolean))reject();break;
                case "events":exact(o,"id","kind","slot","weekday","start","end");choice(o.get("kind"),"Class","Study","Work","Activity","Break");number(o.get("slot"),1,99);number(o.get("weekday"),0,6);number(o.get("start"),0,1439);number(o.get("end"),o.getLong("start")+1,1440);break;
                case "expenses":exact(o,"id","category","amount","day");choice(o.get("category"),"Food","Travel","Learning","Personal","Other");number(o.get("amount"),1,100000000);day(o.get("day"));break;
                case "rewards":exact(o,"id","source","ref","xp","gold","day");choice(o.get("source"),"quest","focus");number(o.get("ref"),0,1000000000);number(o.get("xp"),0,60);number(o.get("gold"),0,6);day(o.get("day"));break;
                default:reject();
                }
            }
        }
    }
}
