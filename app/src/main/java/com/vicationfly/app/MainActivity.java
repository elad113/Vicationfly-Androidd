package com.vicationfly.app;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.json.*;

public class MainActivity extends Activity {
    static final int ORANGE=Color.rgb(255,107,53), INK=Color.rgb(30,34,40), MUTED=Color.rgb(105,108,116), BG=Color.rgb(247,245,240), CARD=Color.WHITE, LINE=Color.rgb(231,228,221), GREEN=Color.rgb(29,155,100);
    LinearLayout root, body, nav;
    String screen="home";
    String from="",to="",depart="",ret="";
    int adults=1,children=0,infants=0;
    String cabin="Economy", tripType="Round trip";

    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,float size,int color){ TextView v=new TextView(this); v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setFontFeatureSettings("kern");return v; }
    TextView label(String s){ TextView v=tv(s.toUpperCase(Locale.US),11,ORANGE);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v; }
    GradientDrawable shape(int color,float r){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(r));return g;}
    GradientDrawable stroke(int color,int line,float r){GradientDrawable g=shape(color,r);g.setStroke(dp(1),line);return g;}
    LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    LinearLayout.LayoutParams weight(int h){return new LinearLayout.LayoutParams(0,h,1);}
    View gap(int h){Space s=new Space(this);s.setLayoutParams(lp(1,dp(h)));return s;}

    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);showSplash();}

    void showSplash(){
        FrameLayout f=new FrameLayout(this); f.setBackgroundColor(BG);
        LinearLayout c=col();c.setGravity(Gravity.CENTER);c.setPadding(dp(30),0,dp(30),0);
        TextView book=tv("✈",58,ORANGE);book.setGravity(17);
        TextView title=tv("Vicationfly",38,INK);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setGravity(17);
        TextView sub=tv("Find the world. One flight at a time.",15,MUTED);sub.setGravity(17);
        c.addView(book,lp(-1,dp(75)));c.addView(title,lp(-1,dp(55)));c.addView(sub,lp(-1,dp(35)));f.addView(c);
        TextView plane=tv("✈",25,ORANGE);FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(dp(45),dp(45));pp.gravity=Gravity.CENTER_VERTICAL;f.addView(plane,pp);
        f.postDelayed(()->{ObjectAnimatorCompat.slide(plane,f,900);},180);
        setContentView(f);f.postDelayed(this::home,1400);
    }

    void base(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);sv.addView(body);root.addView(sv,weight(0));
        nav=bottomNav();root.addView(nav,lp(-1,dp(76)));setContentView(root);
    }

    LinearLayout bottomNav(){
        LinearLayout n=row();n.setPadding(dp(8),dp(8),dp(8),dp(10));n.setBackgroundColor(Color.WHITE);
        String[] names={"Home","Search","Trips","Profile"};String[] icons={"⌂","⌕","▣","●"};
        for(int i=0;i<4;i++){final int ix=i;LinearLayout item=col();item.setGravity(Gravity.CENTER);TextView ic=tv(icons[i],23,screen.equals(screenName(i))?ORANGE:MUTED);TextView tx=tv(names[i],11,screen.equals(screenName(i))?INK:MUTED);item.addView(ic,lp(-1,dp(28)));item.addView(tx,lp(-1,dp(22)));item.setOnClickListener(v->{if(ix==0)home();else if(ix==1)searchScreen();else if(ix==2)trips();else profile();});n.addView(item,weight(dp(58)));}return n;
    }
    String screenName(int i){return i==0?"home":i==1?"search":i==2?"trips":"profile";}

    void home(){
        screen="home";base();
        body.setPadding(dp(20),dp(22),dp(20),dp(24));
        TextView brand=tv("Vicationfly",30,INK);brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(brand,lp(-1,dp(42)));
        TextView sub=tv("Find your next flight.",17,MUTED);body.addView(sub,lp(-1,dp(32)));
        body.addView(gap(12));
        LinearLayout hero=col();hero.setPadding(dp(20),dp(18),dp(20),dp(20));hero.setBackground(shape(INK,28));
        TextView h=tv("Where will you go?",24,Color.WHITE);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);hero.addView(h,lp(-1,dp(36)));
        TextView hs=tv("Search live offers from connected airline and booking partners.",13,0xffC8CBD0);hero.addView(hs,lp(-1,dp(42)));
        body.addView(hero,lp(-1,dp(116)));body.addView(gap(14));
        LinearLayout card=col();card.setPadding(dp(16),dp(16),dp(16),dp(18));card.setBackground(stroke(CARD,LINE,26));
        LinearLayout toggle=row();TextView rt=toggle("Round trip",true),ow=toggle("One way",false);toggle.addView(rt,weight(dp(48)));toggle.addView(ow,weight(dp(48)));card.addView(toggle);
        rt.setOnClickListener(v->{tripType="Round trip";rt.setTextColor(INK);ow.setTextColor(MUTED);});
        ow.setOnClickListener(v->{tripType="One way";ow.setTextColor(INK);rt.setTextColor(MUTED);});
        card.addView(gap(12));card.addView(airportRow("FROM",from.isEmpty()?"Choose departure":from,true),lp(-1,dp(72)));card.addView(gap(8));card.addView(airportRow("TO",to.isEmpty()?"Choose destination":to,false),lp(-1,dp(72)));
        card.addView(gap(10));card.addView(dateRow());card.addView(gap(10));card.addView(passengerRow());card.addView(gap(10));card.addView(cabinRow());card.addView(gap(15));
        Button search=primary("Search flights");search.setOnClickListener(v->validateAndSearch());card.addView(search,lp(-1,dp(56)));
        body.addView(card);body.addView(gap(16));
        LinearLayout note=col();note.setPadding(dp(14),dp(12),dp(14),dp(12));note.setBackground(shape(0xfffff3e7,18));note.addView(tv("LIVE DATA ONLY",11,ORANGE));note.addView(tv("Vicationfly never invents flight availability or prices. Live flight results require a configured backend provider.",12,MUTED));body.addView(note);
    }

    TextView toggle(String s,boolean active){TextView t=tv(s,15,active?INK:MUTED);t.setGravity(17);t.setTypeface(Typeface.DEFAULT,active?Typeface.BOLD:Typeface.NORMAL);return t;}
    LinearLayout airportRow(String l,String value,boolean isFrom){LinearLayout r=col();r.setPadding(dp(14),dp(8),dp(14),dp(7));r.setBackground(shape(0xfff5f3ef,18));r.addView(label(l));TextView v=tv(value,15,value.startsWith("Choose")?MUTED:INK);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);r.addView(v,lp(-1,dp(35)));r.setOnClickListener(x->airportSearch(isFrom));return r;}
    LinearLayout dateRow(){LinearLayout r=row();r.setBackground(shape(0xfff5f3ef,18));r.setPadding(dp(14),0,dp(8),0);LinearLayout d=col();d.addView(label("DATES"));TextView val=tv(depart.isEmpty()?"Departure  •  Return":depart+"  →  "+ret,15,depart.isEmpty()?MUTED:INK);val.setTypeface(Typeface.DEFAULT,Typeface.BOLD);d.addView(val,lp(-1,dp(38)));r.addView(d,weight(dp(66)));Button b=small("Choose");b.setOnClickListener(v->calendar());r.addView(b,lp(dp(88),dp(45)));return r;}
    LinearLayout passengerRow(){LinearLayout r=col();r.setPadding(dp(14),dp(8),dp(14),dp(8));r.setBackground(shape(0xfff5f3ef,18));r.addView(label("TRAVELERS"));TextView v=tv(adults+" adult"+(adults==1?"":"s")+"  •  "+children+" child  •  "+infants+" infant",15,INK);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);r.addView(v,lp(-1,dp(35)));r.setOnClickListener(x->passengers());return r;}
    LinearLayout cabinRow(){LinearLayout r=col();r.setPadding(dp(14),dp(8),dp(14),dp(8));r.setBackground(shape(0xfff5f3ef,18));r.addView(label("CABIN"));TextView v=tv(cabin,15,INK);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);r.addView(v,lp(-1,dp(35)));r.setOnClickListener(x->cabinDialog());return r;}

    Button primary(String s){Button b=new Button(this);b.setText(s);b.setTextSize(16);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(shape(ORANGE,18));return b;}
    Button small(String s){Button b=new Button(this);b.setText(s);b.setTextSize(13);b.setTextColor(INK);b.setAllCaps(false);b.setBackground(stroke(Color.WHITE,LINE,14));return b;}

    void airportSearch(boolean isFrom){
        final Dialog d=new Dialog(this);LinearLayout box=col();box.setPadding(dp(18),dp(18),dp(18),dp(18));box.setBackground(shape(BG,28));
        LinearLayout head=row();TextView x=tv("‹",34,INK);TextView title=tv(isFrom?"Choose departure":"Choose destination",22,INK);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);head.addView(x,lp(dp(45),dp(48)));head.addView(title,weight(dp(48)));box.addView(head);
        EditText q=new EditText(this);q.setSingleLine();q.setHint("Search city, airport, IATA code or country");q.setTextSize(15);q.setPadding(dp(16),0,dp(16),0);q.setBackground(shape(Color.WHITE,17));box.addView(q,lp(-1,dp(54)));box.addView(gap(8));
        LinearLayout list=col();ScrollView sv=new ScrollView(this);sv.addView(list);box.addView(sv,weight(dp(320)));x.setOnClickListener(v->d.dismiss());
        Runnable render=()->{list.removeAllViews();String query=q.getText().toString().trim();ArrayList<String[]> hits=AirportIndex.search(query);if(hits.size()==0){TextView no=tv(query.isEmpty()?"Start typing to search airports":"No airport matches. Connect a live airport index to search globally.",14,MUTED);no.setPadding(dp(8),dp(20),dp(8),dp(20));list.addView(no);return;}for(String[]a:hits){LinearLayout item=row();item.setPadding(dp(10),dp(8),dp(10),dp(8));item.setBackground(stroke(Color.WHITE,LINE,17));LinearLayout info=col();info.addView(tv(a[0],15,INK));info.addView(tv(a[1]+"  •  "+a[2]+"  •  "+a[3],12,MUTED));item.addView(info,weight(dp(68)));item.setOnClickListener(v->{if(isFrom)from=a[2]+" · "+a[1];else to=a[2]+" · "+a[1];d.dismiss();home();});list.addView(item,lp(-1,dp(70)));list.addView(gap(6));}};
        q.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render.run();}public void afterTextChanged(Editable e){}});render.run();
        d.setContentView(box);Window w=d.getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.94f),dp(570));d.show();w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.94f),dp(570));q.requestFocus();w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    void calendar(){
        final Dialog d=new Dialog(this);LinearLayout box=col();box.setPadding(dp(18),dp(18),dp(18),dp(18));box.setBackground(shape(BG,28));
        TextView h=tv("Select dates",25,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(h,lp(-1,dp(40)));
        TextView hint=tv("Choose departure, then return. Both are required.",13,MUTED);box.addView(hint,lp(-1,dp(36)));
        CalendarView cv=new CalendarView(this);cv.setMinDate(System.currentTimeMillis()-1000);box.addView(cv,lp(-1,dp(310)));
        TextView selected=tv("Departure: —     Return: —",14,INK);selected.setPadding(dp(4),dp(10),0,0);box.addView(selected,lp(-1,dp(45)));
        LinearLayout actions=row();Button cancel=small("Cancel"),apply=primary("Apply");apply.setEnabled(false);apply.setAlpha(.45f);actions.addView(cancel,weight(dp(52)));actions.addView(gap(8));actions.addView(apply,weight(dp(52)));box.addView(actions);
        final long[] first={0},second={0};final SimpleDateFormat fmt=new SimpleDateFormat("dd MMM yyyy",Locale.US);
        cv.setOnDateChangeListener((v,y,m,day)->{Calendar c=Calendar.getInstance();c.set(y,m,day,0,0,0);long t=c.getTimeInMillis();if(first[0]==0||second[0]!=0){first[0]=t;second[0]=0;}else if(t<first[0]){second[0]=first[0];first[0]=t;}else second[0]=t;selected.setText("Departure: "+fmt.format(new Date(first[0]))+"     Return: "+(second[0]==0?"—":fmt.format(new Date(second[0]))));apply.setEnabled(second[0]!=0);apply.setAlpha(second[0]!=0?1f:.45f);});
        cancel.setOnClickListener(v->d.dismiss());apply.setOnClickListener(v->{depart=fmt.format(new Date(first[0]));ret=fmt.format(new Date(second[0]));d.dismiss();home();});d.setContentView(box);Window w=d.getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);d.show();w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.94f),dp(500));
    }

    void passengers(){ final Dialog d=new Dialog(this);LinearLayout box=col();box.setPadding(dp(20),dp(20),dp(20),dp(20));box.setBackground(shape(BG,26));TextView h=tv("Travelers",24,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(h);box.addView(gap(12));String[] names={"Adults","Children","Infants"};int[] vals={adults,children,infants};for(int i=0;i<3;i++){LinearLayout r=row();TextView n=tv(names[i],16,INK);r.addView(n,weight(dp(52)));TextView count=tv(""+vals[i],16,INK);Button minus=small("−"),plus=small("+");final int ix=i;minus.setOnClickListener(v->{int z=ix==0?adults:ix==1?children:infants;if(z>(ix==0?1:0)){if(ix==0)adults--;else if(ix==1)children--;else infants--;d.dismiss();passengers();}});plus.setOnClickListener(v->{if(ix==0)adults++;else if(ix==1)children++;else infants++;d.dismiss();passengers();});r.addView(minus,lp(dp(46),dp(45)));r.addView(count,lp(dp(40),dp(45)));r.addView(plus,lp(dp(46),dp(45)));box.addView(r);box.addView(gap(6));}Button done=primary("Done");done.setOnClickListener(v->{d.dismiss();home();});box.addView(done,lp(-1,dp(52)));d.setContentView(box);Window w=d.getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);d.show();w.setLayout((int)(getResources().getDisplayMetrics().widthPixels*.9f),WindowManager.LayoutParams.WRAP_CONTENT);}
    void cabinDialog(){String[] a={"Economy","Premium Economy","Business","First"};new AlertDialog.Builder(this).setTitle("Cabin class").setSingleChoiceItems(a,Arrays.asList(a).indexOf(cabin),(d,w)->{cabin=a[w];d.dismiss();home();}).show();}

    void validateAndSearch(){if(from.isEmpty()||to.isEmpty()){toast("Choose departure and destination.");return;}if(depart.isEmpty()){toast("Choose your departure date.");return;}searchScreen();}

    String airportCode(String v){int p=v.indexOf(" · ");return p>0?v.substring(0,p).trim():v.trim();}
    String apiDate(String v){try{return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new SimpleDateFormat("dd MMM yyyy",Locale.US).parse(v));}catch(Exception e){return v;}}

    void searchScreen(){screen="search";base();body.setPadding(dp(20),dp(20),dp(20),dp(24));TextView h=tv("Flight results",30,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(h,lp(-1,dp(42)));TextView route=tv(airportCode(from)+"  →  "+airportCode(to),15,MUTED);body.addView(route,lp(-1,dp(30)));
        LinearLayout loading=col();loading.setGravity(Gravity.CENTER);loading.setPadding(dp(24),dp(28),dp(24,dp(28)));loading.setBackground(shape(Color.WHITE,24));ProgressBar p=new ProgressBar(this);loading.addView(p,lp(dp(44),dp(44)));TextView a=tv("Searching Flight MCP live cache…",17,INK);a.setGravity(17);loading.addView(a,lp(-1,dp(42)));TextView bb=tv("Real fare data only — no invented flights or prices.",13,MUTED);bb.setGravity(17);loading.addView(bb,lp(-1,dp(36)));body.addView(loading);new Thread(()->fetchFlightMcp()).start();
    }

    void fetchFlightMcp(){try{
        URL u=new URL("https://flight-mcp.com/v1/flights/search/cached");HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(12000);c.setReadTimeout(20000);c.setRequestProperty("Content-Type","application/json");c.setDoOutput(true);
        String json="{\"origin\":\""+airportCode(from)+"\",\"destination\":\""+airportCode(to)+"\",\"departureDate\":\""+apiDate(depart)+"\",\"maxResults\":20,\"cacheTtlSeconds\":604800}";c.getOutputStream().write(json.getBytes("UTF-8"));int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();BufferedReader br=new BufferedReader(new InputStreamReader(in));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);br.close();String payload=sb.toString();if(code<200||code>=300)throw new Exception("Provider returned "+code+": "+payload);JSONObject root=new JSONObject(payload);JSONArray offers=root.optJSONArray("offers");ArrayList<String[]> rows=new ArrayList<>();if(offers!=null)for(int i=0;i<offers.length();i++){JSONObject o=offers.optJSONObject(i);if(o==null)continue;String price=o.optString("price",o.optString("total_amount",""));String currency=o.optString("currency",o.optString("total_currency","USD"));String airline=o.optString("airline",o.optString("carrier",o.optString("airline_name","Airline")));String depTime=o.optString("departureTime",o.optString("departure_time",""));String arrTime=o.optString("arrivalTime",o.optString("arrival_time",""));String duration=o.optString("duration","");String stops=o.optString("stops","");rows.add(new String[]{airline,depTime,arrTime,duration,stops,price,currency});}runOnUiThread(()->showProviderResults(rows));\n    }catch(Exception e){runOnUiThread(()->showProviderError(e.getMessage()));}}

    void showProviderResults(ArrayList<String[]> rows){body.removeAllViews();TextView h=tv("Flight results",29,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(h,lp(-1,dp(42)));body.addView(tv(airportCode(from)+"  →  "+airportCode(to)+"  •  "+depart,14,MUTED),lp(-1,dp(30)));LinearLayout badge=col();badge.setPadding(dp(14),dp(10),dp(14),dp(10));badge.setBackground(shape(0xfffff1e8,16));badge.addView(tv("LIVE PROVIDER · Flight MCP",11,ORANGE));badge.addView(tv("Open Cache data can be up to 7 days old and is limited to supported routes.",12,MUTED));body.addView(badge,lp(-1,dp(68)));body.addView(gap(12));ScrollView sv=new ScrollView(this);LinearLayout list=col();if(rows.size()==0){TextView no=tv("No cached offers for this route/date.",17,INK);no.setGravity(17);list.addView(no,lp(-1,dp(100)));}for(String[]r:rows){LinearLayout card=col();card.setPadding(dp(16),dp(14),dp(16),dp(14));card.setBackground(shape(Color.WHITE,22));TextView al=tv(r[0],17,INK);al.setTypeface(Typeface.DEFAULT,Typeface.BOLD);card.addView(al);card.addView(tv((r[1].isEmpty()?"Departure":r[1])+"  →  "+(r[2].isEmpty()?"Arrival":r[2]),16,INK));card.addView(tv((r[3].isEmpty()?"":r[3]+"  •  ")+(r[4].isEmpty()?"":r[4]+" stops"),12,MUTED));TextView price=tv((r[5].isEmpty()?"Price unavailable":r[5]+" "+r[6]),21,ORANGE);price.setTypeface(Typeface.DEFAULT,Typeface.BOLD);card.addView(price);Button details=small("View offer");details.setOnClickListener(v->toast("Offer details are supplied by the provider. Booking/revalidation is not enabled in Open Cache mode."));card.addView(details,lp(-1,dp(46)));list.addView(card,lp(-1,dp(150)));list.addView(gap(10));}sv.addView(list);body.addView(sv,weight(0));Button back=small("New search");back.setOnClickListener(v->home());body.addView(back,lp(-1,dp(52)));}

    void showProviderError(String msg){body.removeAllViews();TextView h=tv("Flight results",29,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(h,lp(-1,dp(42)));LinearLayout e=col();e.setGravity(Gravity.CENTER);e.setPadding(dp(22),dp(30),dp(22),dp(30));e.setBackground(shape(Color.WHITE,24));TextView t=tv("No live offer available",19,INK);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(17);e.addView(t,lp(-1,dp(45)));TextView m=tv("The zero-setup provider is cached and route-limited. For worldwide fresh fares and booking, Vicationfly needs a server-side Duffel access token.",14,MUTED);m.setGravity(17);e.addView(m,lp(-1,dp(90)));Button bb=small("Back to search");bb.setOnClickListener(v->home());e.addView(bb,lp(-1,dp(50)));body.addView(e);}

    void backendInfo(){new AlertDialog.Builder(this).setTitle("Flight provider").setMessage("Default provider: Flight MCP Open Cache. It requires no API key, but it is cached and limited to supported routes. Production worldwide live search should use Duffel through a secure server-side access token; never embed that token in the APK.").setPositiveButton("OK",null).show();}
    void trips(){screen="trips";base();body.setPadding(dp(20),dp(22),dp(20),dp(24));TextView h=tv("My Trips",30,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(h,lp(-1,dp(45)));LinearLayout card=col();card.setGravity(Gravity.CENTER);card.setPadding(dp(22),dp(40),dp(22),dp(40));card.setBackground(shape(Color.WHITE,24));TextView i=tv("▣",42,ORANGE);i.setGravity(17);card.addView(i,lp(-1,dp(58)));TextView a=tv("No bookings yet",20,INK);a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);a.setGravity(17);card.addView(a,lp(-1,dp(42)));TextView b=tv("Your confirmed trips will appear here after a real provider booking is completed.",13,MUTED);b.setGravity(17);card.addView(b,lp(-1,dp(58)));body.addView(card);}
    void profile(){screen="profile";base();body.setPadding(dp(20),dp(22),dp(20),dp(24));TextView h=tv("Profile",30,INK);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(h,lp(-1,dp(45)));setting("Language","English / עברית");setting("Currency","ILS · USD · EUR · GBP");setting("Notifications","Manage travel alerts");setting("Privacy","Secure payments and data controls");setting("About","Vicationfly flight search & booking intermediary");}
    void setting(String a,String b){LinearLayout c=col();c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(shape(Color.WHITE,20));c.addView(tv(a,16,INK));c.addView(tv(b,12,MUTED));body.addView(c,lp(-1,dp(72)));body.addView(gap(8));}

    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}

class ObjectAnimatorCompat {
    static void slide(final View v, final View parent, long duration){
        final int w=parent.getWidth()>0?parent.getWidth():1080;
        v.setTranslationX(-w/2f);v.animate().translationX(w/2f).setDuration(duration).withEndAction(()->{v.setTranslationX(-w/2f);slide(v,parent,duration);}).start();
    }
}
class AirportIndex {
    static final String[][] DATA={
        {"Tel Aviv","Ben Gurion Airport","TLV","Israel"},{"London","Heathrow Airport","LHR","United Kingdom"},{"London","Gatwick Airport","LGW","United Kingdom"},
        {"Paris","Charles de Gaulle Airport","CDG","France"},{"Paris","Orly Airport","ORY","France"},{"Amsterdam","Schiphol Airport","AMS","Netherlands"},
        {"Rome","Fiumicino Airport","FCO","Italy"},{"Madrid","Adolfo Suarez Madrid-Barajas","MAD","Spain"},{"Barcelona","El Prat Airport","BCN","Spain"},
        {"Athens","Athens International Airport","ATH","Greece"},{"Berlin","Brandenburg Airport","BER","Germany"},{"Frankfurt","Frankfurt Airport","FRA","Germany"},
        {"Dubai","Dubai International Airport","DXB","United Arab Emirates"},{"Doha","Hamad International Airport","DOH","Qatar"},{"Abu Dhabi","Zayed International Airport","AUH","United Arab Emirates"},
        {"New York","John F. Kennedy International","JFK","United States"},{"New York","LaGuardia Airport","LGA","United States"},{"Los Angeles","Los Angeles International","LAX","United States"},
        {"San Francisco","San Francisco International","SFO","United States"},{"Miami","Miami International Airport","MIA","United States"},{"Chicago","O'Hare International Airport","ORD","United States"},
        {"Toronto","Toronto Pearson International","YYZ","Canada"},{"Vancouver","Vancouver International","YVR","Canada"},{"Mexico City","Mexico City International","MEX","Mexico"},
        {"Tokyo","Haneda Airport","HND","Japan"},{"Tokyo","Narita International","NRT","Japan"},{"Seoul","Incheon International","ICN","South Korea"},
        {"Singapore","Changi Airport","SIN","Singapore"},{"Bangkok","Suvarnabhumi Airport","BKK","Thailand"},{"Hong Kong","Hong Kong International","HKG","Hong Kong"},
        {"Sydney","Sydney Kingsford Smith","SYD","Australia"},{"Melbourne","Melbourne Airport","MEL","Australia"},{"Auckland","Auckland Airport","AKL","New Zealand"},
        {"Mumbai","Chhatrapati Shivaji Maharaj International","BOM","India"},{"Delhi","Indira Gandhi International","DEL","India"},{"Cairo","Cairo International","CAI","Egypt"},
        {"Istanbul","Istanbul Airport","IST","Türkiye"},{"Lisbon","Humberto Delgado Airport","LIS","Portugal"},{"Zurich","Zurich Airport","ZRH","Switzerland"},
        {"Vienna","Vienna International","VIE","Austria"},{"Prague","Vaclav Havel Airport Prague","PRG","Czechia"},{"Copenhagen","Copenhagen Airport","CPH","Denmark"},
        {"Stockholm","Arlanda Airport","ARN","Sweden"},{"Oslo","Oslo Airport","OSL","Norway"},{"Helsinki","Helsinki Airport","HEL","Finland"},
        {"Warsaw","Warsaw Chopin Airport","WAW","Poland"},{"Budapest","Budapest Ferenc Liszt International","BUD","Hungary"},{"Bucharest","Henri Coanda International","OTP","Romania"},
        {"Johannesburg","O. R. Tambo International","JNB","South Africa"},{"Cape Town","Cape Town International","CPT","South Africa"},{"Nairobi","Jomo Kenyatta International","NBO","Kenya"},
        {"Riyadh","King Khalid International","RUH","Saudi Arabia"},{"Jeddah","King Abdulaziz International","JED","Saudi Arabia"},{"Muscat","Muscat International","MCT","Oman"},
        {"Kuala Lumpur","Kuala Lumpur International","KUL","Malaysia"},{"Jakarta","Soekarno-Hatta International","CGK","Indonesia"},{"Manila","Ninoy Aquino International","MNL","Philippines"},
        {"Sao Paulo","Guarulhos International","GRU","Brazil"},{"Buenos Aires","Ezeiza International","EZE","Argentina"},{"Santiago","Arturo Merino Benitez","SCL","Chile"},
        {"Lima","Jorge Chavez International","LIM","Peru"},{"Reykjavik","Keflavik International","KEF","Iceland"},{"Dublin","Dublin Airport","DUB","Ireland"}
    };
    static ArrayList<String[]> search(String q){ArrayList<String[]> out=new ArrayList<>();String z=q.toLowerCase(Locale.US);for(String[]a:DATA){String all=(a[0]+" "+a[1]+" "+a[2]+" "+a[3]).toLowerCase(Locale.US);if(z.isEmpty()||all.contains(z))out.add(a);if(out.size()==5)break;}return out;}
}
