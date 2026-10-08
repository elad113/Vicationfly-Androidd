package com.vicationfly.fresh;

import android.app.*;
import android.os.*;
import android.provider.Settings;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import android.text.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    static final int PICK_IMAGE=7001;
    final int ORANGE=Color.rgb(255,107,0), DARK=Color.rgb(32,35,42), CREAM=Color.rgb(255,249,243), MUTED=Color.rgb(111,114,121), GREEN=Color.rgb(32,150,100);
    LinearLayout root, body, homeList; ImageView activePreview; String activeImageUri="";
    ArrayList<Vication> trips=new ArrayList<>();
    String selectedTripId="";
    SharedPreferences prefs;

    static class Vication {
        String id,name,image,created;
        Vication(String i,String n,String im,String c){id=i;name=n;image=im;created=c;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(CREAM); getWindow().setNavigationBarColor(CREAM);
        prefs=getSharedPreferences("vications",MODE_PRIVATE);
        loadTrips();
        showSplash();
    }

    TextView tv(String s,float z,int c){ TextView t=new TextView(this); t.setText(s); t.setTextSize(z); t.setTextColor(c); return t; }
    GradientDrawable bg(int c,float r){ GradientDrawable g=new GradientDrawable(); g.setColor(c); g.setCornerRadius(r); return g; }
    LinearLayout card(int c,float r){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setBackground(bg(c,r)); return l; }
    LinearLayout.LayoutParams lp(int w,int h,float wt){ return new LinearLayout.LayoutParams(w,h,wt); }
    Space gap(int h){ Space s=new Space(this); s.setLayoutParams(lp(1,h,0)); return s; }
    Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackground(bg(ORANGE,22)); b.setPadding(18,0,18,0); return b; }
    TextView label(String s){ TextView t=tv(s,11,ORANGE); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return t; }

    void showSplash(){
        FrameLayout f=new FrameLayout(this); f.setBackgroundColor(CREAM);
        LinearLayout center=new LinearLayout(this); center.setOrientation(LinearLayout.VERTICAL); center.setGravity(Gravity.CENTER);
        TextView logo=tv("✈",58,ORANGE); logo.setGravity(Gravity.CENTER);
        TextView name=tv("Vicationfly",35,DARK); name.setGravity(Gravity.CENTER); name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView sub=tv("Plan it. Fly it. Remember it.",15,MUTED); sub.setGravity(Gravity.CENTER);
        center.addView(logo); center.addView(name); center.addView(sub);
        f.addView(center,new FrameLayout.LayoutParams(-1,-1));
        TextView plane=tv("✈",34,ORANGE);
        FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(70,60); pp.gravity=Gravity.BOTTOM; pp.bottomMargin=110; f.addView(plane,pp);
        setContentView(f);
        plane.postDelayed(new Runnable(){ public void run(){
            float w=f.getWidth()+80; plane.setTranslationX(-90); plane.animate().translationX(w).setDuration(1300).withEndAction(()->{
                plane.setTranslationX(-90); plane.postDelayed(this,120);
            }).start();
        }},120);
        f.postDelayed(()->{ plane.animate().cancel(); showHome(); },2100);
    }

    void base(String title, String subtitle){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(CREAM);
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.VERTICAL); top.setPadding(22,20,22,8);
        TextView h=tv(title,30,DARK); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); top.addView(h);
        if(subtitle!=null) top.addView(tv(subtitle,15,MUTED),lp(-1,30,0));
        root.addView(top,lp(-1,-2,0));
        ScrollView sv=new ScrollView(this); sv.setFillViewport(true);
        body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(22,10,22,28); sv.addView(body);
        root.addView(sv,lp(-1,0,1)); setContentView(root);
    }

    void showHome(){
        base("Vicationfly","Your trips, all in one place.");
        Button create=button("+  Create Vication"); create.setTextSize(18); create.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        create.setOnClickListener(v->showCreate()); body.addView(create,lp(-1,64,0)); body.addView(gap(22));
        TextView section=tv("Your Vications",20,DARK); section.setTypeface(Typeface.DEFAULT,Typeface.BOLD); body.addView(section); body.addView(gap(10));
        homeList=body;
        if(trips.isEmpty()){
            LinearLayout empty=card(Color.WHITE,28); empty.setGravity(Gravity.CENTER); empty.setPadding(24,34,24,34);
            TextView icon=tv("✈",40,ORANGE); icon.setGravity(Gravity.CENTER); empty.addView(icon,lp(-1,55,0));
            TextView a=tv("No Vications yet",21,DARK); a.setTypeface(Typeface.DEFAULT,Typeface.BOLD); a.setGravity(Gravity.CENTER); empty.addView(a);
            TextView b=tv("Create your first Vication and start planning your trip.",14,MUTED); b.setGravity(Gravity.CENTER); b.setPadding(15,8,15,0); empty.addView(b);
            body.addView(empty,lp(-1,-2,0));
        } else for(Vication x:trips) addTripCard(x);
    }

    void addTripCard(Vication x){
        LinearLayout c=card(Color.WHITE,28); c.setPadding(0,0,0,14);
        TextView image=tv(x.image.isEmpty()?"": " ",1,Color.WHITE); image.setGravity(Gravity.CENTER);
        image.setBackgroundColor(Color.WHITE);
        c.addView(image,lp(-1,145,0));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(18,13,18,5);
        TextView n=tv(x.name,20,DARK); n.setTypeface(Typeface.DEFAULT,Typeface.BOLD); info.addView(n);
        info.addView(tv("Vication • "+x.created,12,MUTED)); c.addView(info);
        c.setOnClickListener(v->{selectedTripId=x.id;showFindFlights(x);});
        body.addView(c,lp(-1,-2,0)); body.addView(gap(14));
    }

    void showCreate(){
        base("Create your Vication","Give your trip a name and an optional picture.");
        body.addView(label("VICATION NAME")); body.addView(gap(5));
        EditText name=new EditText(this); name.setHint("Summer in London"); name.setTextSize(17); name.setSingleLine(); name.setPadding(17,0,17,0); name.setBackground(bg(Color.WHITE,20)); body.addView(name,lp(-1,60,0)); body.addView(gap(20));
        body.addView(label("COVER PICTURE")); body.addView(gap(8));
        LinearLayout preview=card(Color.WHITE,24); preview.setGravity(Gravity.CENTER); TextView pt=tv("＋\nAdd a picture",17,MUTED); pt.setGravity(Gravity.CENTER); preview.addView(pt,lp(-1,180,0)); body.addView(preview,lp(-1,-2,0));
        final String[] chosen={""};
        preview.setOnClickListener(v->pickImage(preview,pt,chosen)); body.addView(gap(28));
        Button save=button("Save Vication"); save.setTextSize(17); save.setOnClickListener(v->{
            String n=name.getText().toString().trim(); if(n.isEmpty()){name.setError("Enter a Vication name");return;}
            if(chosen[0].isEmpty() && !activeImageUri.isEmpty()) chosen[0]=activeImageUri; String id=UUID.randomUUID().toString(); String now=new SimpleDateFormat("d MMM yyyy",Locale.US).format(new Date());
            trips.add(0,new Vication(id,n,chosen[0],now)); persist(); showHome();
        }); body.addView(save,lp(-1,60,0));
    }

    void pickImage(LinearLayout preview,TextView pt,String[] chosen){
        Intent i;
        if(Build.VERSION.SDK_INT>=33) i=new Intent("android.provider.action.PICK_IMAGES"); else {i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);}
        try{startActivityForResult(i,PICK_IMAGE);}catch(Exception e){i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(i,PICK_IMAGE);}
    }
    @Override protected void onActivityResult(int r,int c,Intent d){
        super.onActivityResult(r,c,d); if(r==PICK_IMAGE&&c==RESULT_OK&&d!=null&&d.getData()!=null){
            Uri u=d.getData(); getSharedPreferences("imageuri",0).edit().putString("last",u.toString()).apply();
            Toast.makeText(this,"Picture added",Toast.LENGTH_SHORT).show();
        }
    }

    void showFindFlights(Vication trip){
        base("Find Flights",trip.name);
        body.addView(label("TRIP TYPE")); body.addView(gap(5));
        LinearLayout toggle=card(Color.WHITE,20); toggle.setOrientation(LinearLayout.HORIZONTAL); toggle.setPadding(5,5,5,5);
        Button round=button("Round Trip"), one=button("One Way"); round.setTextSize(14); one.setTextSize(14);
        toggle.addView(round,lp(0,50,1)); toggle.addView(one,lp(0,50,1)); body.addView(toggle,lp(-1,60,0)); body.addView(gap(15));
        EditText from=field("From — city, airport or IATA"); EditText to=field("To — city, airport or IATA");
        body.addView(label("FROM"));body.addView(gap(4));body.addView(from,lp(-1,60,0));body.addView(gap(12));body.addView(label("TO"));body.addView(gap(4));body.addView(to,lp(-1,60,0));body.addView(gap(14));
        TextView dep=choice("Departure"); TextView ret=choice("Return"); TextView pass=choice("Passengers • 1 adult"); TextView cabin=choice("Cabin • Economy");
        body.addView(label("DATES"));body.addView(gap(4));body.addView(dep,lp(-1,58,0));body.addView(gap(8));body.addView(ret,lp(-1,58,0));body.addView(gap(12));
        body.addView(pass,lp(-1,58,0));body.addView(gap(8));body.addView(cabin,lp(-1,58,0));body.addView(gap(24));
        final boolean[] rt={true}; round.setOnClickListener(v->{rt[0]=true;round.setTextColor(Color.WHITE);one.setTextColor(MUTED);}); one.setOnClickListener(v->{rt[0]=false;round.setTextColor(MUTED);one.setTextColor(Color.WHITE);ret.setVisibility(View.GONE);});
        final String[] dates={""}; dep.setOnClickListener(v->pickDate(dep,dates,false,ret)); ret.setOnClickListener(v->pickDate(ret,dates,true,null));
        Button find=button("Find Flights");find.setTextSize(18);find.setOnClickListener(v->{
            if(from.getText().length()==0||to.getText().length()==0||dates[0].isEmpty()){Toast.makeText(this,"Complete From, To and dates first",Toast.LENGTH_SHORT).show();return;}
            showResults(trip,from.getText().toString(),to.getText().toString(),dates[0],rt[0]);
        });body.addView(find,lp(-1,62,0));
    }

    EditText field(String h){EditText e=new EditText(this);e.setHint(h);e.setTextSize(16);e.setSingleLine();e.setPadding(17,0,17,0);e.setBackground(bg(Color.WHITE,20));return e;}
    TextView choice(String s){TextView t=tv(s,16,DARK);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(17,0,17,0);t.setBackground(bg(Color.WHITE,20));return t;}
    void pickDate(TextView target,String[] dates,boolean isReturn,TextView other){
        Calendar c=Calendar.getInstance(); DatePickerDialog d=new DatePickerDialog(this,(v,y,m,day)->{
            String z=String.format(Locale.US,"%02d %s %04d",day,new SimpleDateFormat("MMM",Locale.US).format(new Date(y-1900,m,1)),y);
            if(!isReturn){dates[0]=z+" →";target.setText("Departure • "+z);if(other!=null)other.setText("Return");}
            else {dates[0]=dates[0].replace(" →","")+" → "+z;target.setText("Return • "+z);}
        },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));d.setTitle(isReturn?"Return date":"Departure date");d.show();
    }

    void showResults(Vication trip,String from,String to,String date,boolean roundTrip){
        base("Flight Results",trip.name);
        TextView q=tv(from+"  →  "+to,18,DARK);q.setTypeface(Typeface.DEFAULT,Typeface.BOLD);body.addView(q);body.addView(tv(roundTrip?"Round Trip":"One Way",13,MUTED));body.addView(gap(15));
        LinearLayout notice=card(Color.WHITE,22);notice.setPadding(17,15,17,15);notice.addView(tv("Live flight provider required",17,DARK));notice.addView(gap(5));notice.addView(tv("This build never invents airlines, schedules or prices. Connect an Amadeus, Duffel, Travelport, Sabre or NDC backend to load real-time inventory and prices.",13,MUTED));body.addView(notice,lp(-1,-2,0));body.addView(gap(14));
        TextView empty=tv("No live flights loaded",18,MUTED);empty.setGravity(Gravity.CENTER);body.addView(empty,lp(-1,100,0));
        Button details=button("Flight Details");details.setOnClickListener(v->showFlightDetails(trip,from,to,date,roundTrip));body.addView(details,lp(-1,56,0));
    }

    void showFlightDetails(Vication trip,String from,String to,String date,boolean rt){
        base("Flight Details",trip.name);
        LinearLayout c=card(Color.WHITE,25);c.setPadding(18,18,18,18);
        c.addView(tv("LIVE INVENTORY",11,ORANGE));c.addView(gap(8));c.addView(tv(from+"  →  "+to,21,DARK));c.addView(tv(date,14,MUTED));if(rt)c.addView(tv("Return flight required for Round Trip",14,MUTED));body.addView(c,lp(-1,-2,0));body.addView(gap(16));
        TextView msg=tv("No flight can be booked until a real provider returns an available itinerary and a current price.",15,MUTED);msg.setGravity(Gravity.CENTER);body.addView(msg,lp(-1,90,0));
        Button cont=button("Continue");cont.setOnClickListener(v->showPassenger(trip));body.addView(cont,lp(-1,58,0));
    }

    void showPassenger(Vication trip){
        base("Passenger Details",trip.name);
        EditText fn=field("First name");EditText ln=field("Last name");EditText em=field("Email");body.addView(label("PASSENGER"));body.addView(gap(6));body.addView(fn,lp(-1,58,0));body.addView(gap(9));body.addView(ln,lp(-1,58,0));body.addView(gap(9));body.addView(em,lp(-1,58,0));body.addView(gap(22));
        Button next=button("Continue to Payment");next.setOnClickListener(v->{if(fn.length()==0||ln.length()==0||em.length()==0){Toast.makeText(this,"Complete passenger details",Toast.LENGTH_SHORT).show();return;}showPayment(trip);});body.addView(next,lp(-1,60,0));
    }

    void showPayment(Vication trip){
        base("Payment",trip.name);
        LinearLayout secure=card(Color.WHITE,22);secure.setPadding(18,18,18,18);secure.addView(tv("Secure payment",18,DARK));secure.addView(gap(4));secure.addView(tv("Card details are tokenized by your payment provider. This app does not store full card numbers.",13,MUTED));body.addView(secure,lp(-1,-2,0));body.addView(gap(16));
        EditText card=field("Card number");EditText exp=field("MM / YY");EditText cvv=field("CVV");body.addView(card,lp(-1,58,0));body.addView(gap(9));body.addView(exp,lp(-1,58,0));body.addView(gap(9));body.addView(cvv,lp(-1,58,0));body.addView(gap(18));
        Button pay=button("Pay securely");pay.setOnClickListener(v->showConfirmation(trip));body.addView(pay,lp(-1,60,0));
        Button pp=button("PayPal");pp.setOnClickListener(v->showConfirmation(trip));body.addView(pp,lp(-1,55,0));
    }

    void showConfirmation(Vication trip){
        base("Booking Confirmed ✓",trip.name);
        LinearLayout c=card(Color.WHITE,28);c.setPadding(20,22,20,22);TextView ok=tv("✓",45,GREEN);ok.setGravity(Gravity.CENTER);c.addView(ok,lp(-1,60,0));TextView h=tv("Your booking is confirmed",22,DARK);h.setGravity(Gravity.CENTER);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);c.addView(h);c.addView(gap(8));TextView p=tv("Booking reference\nVF-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.US),15,MUTED);p.setGravity(Gravity.CENTER);c.addView(p);body.addView(c,lp(-1,-2,0));body.addView(gap(18));Button home=button("Back to Vications");home.setOnClickListener(v->showHome());body.addView(home,lp(-1,60,0));
    }

    void loadTrips(){String raw=prefs.getString("data","");if(raw.isEmpty())return;for(String row:raw.split("\\|", -1)){String[] z=row.split("~",-1);if(z.length>=4)trips.add(new Vication(z[0],z[1],z[2],z[3]));}}
    void persist(){StringBuilder b=new StringBuilder();for(Vication x:trips){if(b.length()>0)b.append("|");b.append(x.id).append("~").append(x.name.replace("~"," ")).append("~").append(x.image).append("~").append(x.created);}prefs.edit().putString("data",b.toString()).apply();}
}