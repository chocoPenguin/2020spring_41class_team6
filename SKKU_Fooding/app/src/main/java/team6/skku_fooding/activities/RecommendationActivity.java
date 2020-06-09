package team6.skku_fooding.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import team6.skku_fooding.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.*;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class RecommendationActivity extends AppCompatActivity {

    public class Review {

        Double rate;
        Integer product_id;

        public Review() {

        }

        public Double getRate() {
            return rate;
        }

        public Integer getProduct_id() {
            return product_id;
        }
    }

    public class dub {
        Double rate;
        Integer count;

        public dub(){

        }

        public Double getRate() {
            return rate;
        }

        public Integer getCount() {
            return count;
        }
    }

    public class Product {
        Integer product_id;
        Double avgrate;

        public Product() {

        }

        public Integer getProduct_id() {
            return product_id;
        }

        public Double getAvgrate() {
            return avgrate;
        }
    }

    public class information {

        String name;
        String image;
        String ingredient;
        Double rate;
        String company;
        Integer price;

    }

    private DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
    ArrayList<Review> rlist = new ArrayList<Review>();







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);
        getcid();

        Button button = (Button)findViewById(R.id.change);
        Button setrate = (Button)findViewById(R.id.criteria);

        setrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();


            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SurveyActivity.class);

                intent.putExtra("name","R");

                startActivity(intent);
            }
        });




    }

    public void show() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Change rate criteria");
        alert.setMessage("Input your rate criteria");


        final EditText criteria = new EditText(this);
        alert.setView(criteria);

        alert.setPositiveButton("save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String str_cri = criteria.getText().toString();
                Double cri = 0.0;
                boolean flag = true;
                try {
                    cri = Double.parseDouble(str_cri);
                }
                catch(Exception e) {
                    errshow();
                    flag = false;

                }
                if(cri > 5.0 || cri < 0.0) {
                    errshow();
                    flag = false;
                }
                if(flag) {
                    DatabaseReference d = FirebaseDatabase.getInstance().getReference();
                    d.child("user").child("3mOXMr3hU6XYJ7l2aVAIwQBdLDp1").child("criteria").setValue(cri);
                    Intent in1 = new Intent(getApplicationContext(), RecommendationActivity.class);
                    startActivity(in1);
                }


            }
        });


        alert.show();
    }

    public void errshow() {
        Toast erring = Toast.makeText(this.getApplicationContext(), "소수로 입력해주세요 ex) 3.5", Toast.LENGTH_SHORT);
        erring.show();
    }

    public void getcid() {

        dbref.child("user").child("3mOXMr3hU6XYJ7l2aVAIwQBdLDp1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer category_id = dataSnapshot.child("category_id").getValue(Integer.class);

                /* 강제종료 등의 이유로 survey가 skip되었을 때
                if (category_id == null) {
                    Intent gosurvey = new Intent(getApplicationContext(), SurveyActivity.class);
                    gosurvey.putExtra("name", "R");
                    startActivity(gosurvey);
                }

                 */


                Double criteria = dataSnapshot.child("criteria").getValue(Double.class);
                String filter = dataSnapshot.child("filter").getValue(String.class);
                Log.d("value", "category_id" + category_id);
                getreview(category_id, criteria, filter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });


    }

    public void getreview(Integer cid, final Double criteria, final String filter) {
        dbref.child("review").orderByChild("category_id").equalTo(cid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot shot : dataSnapshot.getChildren()) {
                    Review review1 = new Review();
                    review1.rate = shot.child("rate").getValue(Double.class);
                    review1.product_id = shot.child("product_id").getValue(Integer.class);
                    rlist.add(review1);


                }
                Map<Integer, dub> map = new HashMap<Integer, dub>();
                map.clear();
                for (Review r : rlist) {
                    if (map.containsKey(r.product_id)) {
                        dub gets = new dub();
                        gets = map.get(r.product_id);
                        gets.rate = (r.rate + gets.rate * gets.count) / (1+gets.count);
                        gets.count = gets.count + 1;
                        map.put(r.product_id, gets);
                    }
                    else {
                        dub puts = new dub();
                        puts.rate = r.rate;
                        puts.count = 1;
                        map.put(r.product_id, puts);
                    }

                }
                List<Product> plist = new ArrayList<Product>();
                plist.clear();
                for (Map.Entry<Integer, dub> entry : map.entrySet()) {
                    if (entry.getValue().rate > criteria) {
                        Product product = new Product();
                        product.avgrate = entry.getValue().rate;
                        product.product_id = entry.getKey();
                        plist.add(product);

                    }
                }
                for (Product p : plist) {
                    Log.d("List", "recommend_id : "+p.product_id + " rate : " +p.avgrate);
                }
                getProduct(plist, filter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    public void getProduct(List<Product> productList, final String filter) {

        for (final Product p : productList) {
            final Double r = p.avgrate;
            dbref.child("product").orderByChild("product_id").equalTo(p.product_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot shot : dataSnapshot.getChildren()) {
                        information i = new information();

                        i.ingredient = shot.child("ingredient").getValue(String.class);
                        if (applyfilter(filter, i.ingredient)) {
                            i.image = shot.child("image").getValue(String.class);
                            i.name = shot.child("name").getValue(String.class);
                            i.company = shot.child("company").getValue(String.class);
                            i.price = shot.child("price").getValue(Integer.class);
                            i.rate = r;

                            //리스트뷰 추가 시점
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public boolean applyfilter(String f, String i) {//f = filter 문자열, i = ingredient 문자열
        String[] array1 = f.split(",");
        String[] array2 = i.split(",");
        for (String a : array1) {
            for (String b : array2) {
                if(a.equals(b)) {
                    return false; //1개라도 일치하면 false
                }
            }
        }
        return true;
    }
}
