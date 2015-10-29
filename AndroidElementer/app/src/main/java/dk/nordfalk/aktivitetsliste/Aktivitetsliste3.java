package dk.nordfalk.aktivitetsliste;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

import dk.nordfalk.android.elementer.R;

public class Aktivitetsliste3 extends AppCompatActivity {
  int onStartTæller;
  ToggleButton seKildekodeToggleButton;
  ViewPager viewPager;
  private SharedPreferences prefs;
  private String sidstKlikketPåAkt;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Aktivitetsdata.instans.init(getApplication());
    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    viewPager = new ViewPager(this);
    viewPager.setId(R.id.viewPager);
    viewPager.setAdapter(new VPAdapter(getSupportFragmentManager()));
    viewPager.setPageTransformer(false, new ZoomOutPageTransformer());

    PagerSlidingTabStrip pagerSlidingTabStrip = new PagerSlidingTabStrip(this);
    pagerSlidingTabStrip.setViewPager(viewPager);

    LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.addView(pagerSlidingTabStrip);
    ll.addView(viewPager);
    ((LinearLayout.LayoutParams) viewPager.getLayoutParams()).weight = 1;
    setContentView(ll);

    seKildekodeToggleButton = new ToggleButton(this);
    seKildekodeToggleButton.setTextOff("Se kilde");
    seKildekodeToggleButton.setTextOn("Se kilde");
    seKildekodeToggleButton.setChecked(false);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setCustomView(seKildekodeToggleButton);


    seKildekodeToggleButton.setId(119);


    if (savedInstanceState == null) // Frisk start - vis animation
    {
      //viewPager.startAnimation(AnimationUtils.loadAnimation(this, R.anim.egen_anim2));
      // Genskab valg fra sidst der blev startet en aktivitet
      viewPager.setCurrentItem(prefs.getInt("kategoriPos", 1));
      sidstKlikketPåAkt = prefs.getString("sidstKlikketPåAkt","ukendt");
    }
  }



  private void visDialog(String tekst) {
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle("Kunne ikke starte");
    TextView tv = new TextView(this);
    tv.setText(tekst);
    dialog.setView(tv);
    dialog.show();
  }


  void visKildekode(String klasse) {
    String filnavn = klasse;
    if (!filnavn.equals("AndroidManifest.xml")) {
      filnavn = "java/" + filnavn;
      if (!filnavn.endsWith(".java")) {
        filnavn = filnavn.replace('.', '/') + ".java";
      }
    }

    Toast.makeText(this, "Viser " + filnavn, Toast.LENGTH_LONG).show();

    Intent i = new Intent(this, VisKildekode.class);
    i.putExtra(VisKildekode.KILDEKODE_FILNAVN, filnavn);
    startActivity(i);
  }



  private class VPAdapter extends FragmentPagerAdapter {
    public VPAdapter(FragmentManager fm) {
      super(fm);
    }
    // ArrayAdapter(this, android.R.layout.simple_gallery_item, android.R.id.text1, Aktivitetsdata.instans.pakkekategorier)
    @Override
    public int getCount() {
      return Aktivitetsdata.instans.pakkekategorier.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return Aktivitetsdata.instans.pakkekategorier.get(position).replace('_','\n');
    }

    /*
    @Override
    public float getPageWidth(int position) {
      return 0.95f;
    }
    */

    @Override
    public Fragment getItem(int position) {
      Fragment f = new KarruselFrag();
      Bundle b = new Bundle();
      b.putInt("position", position);
      f.setArguments(b);
      return f;
    }
  }

  public static class KarruselFrag extends Fragment implements OnItemClickListener, OnItemLongClickListener {
    ArrayList<String> klasserDerVisesNu = new ArrayList<>();
    private int kategoriPos;
    private Aktivitetsliste3 akt;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      akt = (Aktivitetsliste3) getActivity();
      kategoriPos = getArguments().getInt("position");
      Aktivitetsdata.instans.tjekForAndreFilerIPakken(kategoriPos);
      klasserDerVisesNu.clear();
      klasserDerVisesNu.addAll(Aktivitetsdata.instans.klasselister.get(kategoriPos));

      // Anonym nedarving af ArrayAdapter med omdefineret getView()
      ArrayAdapter<String> klasserDerVisesNuAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, klasserDerVisesNu) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          View view = super.getView(position, convertView, parent);
          TextView listeelem_overskrift = (TextView) view.findViewById(android.R.id.text1);
          TextView listeelem_beskrivelse = (TextView) view.findViewById(android.R.id.text2);

          String pakkeOgKlasse = klasserDerVisesNu.get(position);
          if (pakkeOgKlasse.endsWith(".java")) {
            String pakkenavn = pakkeOgKlasse.substring(0, pakkeOgKlasse.lastIndexOf('/'));
            String klassenavn = pakkeOgKlasse.substring(pakkenavn.length() + 1);
            listeelem_overskrift.setText(klassenavn);
            listeelem_beskrivelse.setText(pakkenavn+"."+klassenavn.substring(0,klassenavn.length()-5)); // fjern .java
          } else if (pakkeOgKlasse.endsWith(".xml")) {
            listeelem_overskrift.setText(pakkeOgKlasse);
            listeelem_beskrivelse.setText("");
          } else {
            String pakkenavn = pakkeOgKlasse.substring(0, pakkeOgKlasse.lastIndexOf('.'));
            String klassenavn = pakkeOgKlasse.substring(pakkenavn.length() + 1);
            listeelem_overskrift.setText(klassenavn);
            listeelem_beskrivelse.setText(pakkeOgKlasse);
          }

          return view;
        }
      };
      ListView visKlasserListView = new ListView(getActivity());
      visKlasserListView.setAdapter(klasserDerVisesNuAdapter);

      visKlasserListView.setOnItemClickListener(this);
      visKlasserListView.setOnItemLongClickListener(this);

      int position = klasserDerVisesNu.indexOf(akt.sidstKlikketPåAkt);
      if (position>0) {
        visKlasserListView.setSelectionFromTop(position, 30);
      }

      return visKlasserListView;
    }


    public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
      String aktKlik = klasserDerVisesNu.get(position);

      if (this.akt.seKildekodeToggleButton.isChecked() || aktKlik.endsWith(".java") || aktKlik.endsWith(".xml")) {
        this.akt.visKildekode(aktKlik);
        return;
      }

      try {
        // Tjek at klassen faktisk kan indlæses (så prg ikke crasher hvis den ikke kan!)
        Class klasse = Class.forName(aktKlik);

        /*
        if (akt.toLowerCase().contains("fragment") && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
          akt.visDialog("Denne aktivitet kan kun køre på Android 4\nSkal den køre på Android 2 skal et kompatibilitetsbibliotek inkluderes og koden ændres til at bruge kompatibilitetsbiblioteket.");
          return;
        }
        */
        startActivity(new Intent(getActivity(), klasse));
        this.akt.overridePendingTransition(0, 0); // hurtigt skift
        Toast.makeText(getActivity(), aktKlik + " startet", Toast.LENGTH_SHORT).show();
      } catch (Throwable e) {
        e.printStackTrace();
        //while (e.getCause() != null) e = e.getCause(); // Hop hen til grunden
        String tekst = aktKlik + " gav fejlen:\n" + Log.getStackTraceString(e);
        this.akt.visDialog(tekst);
      }

      // Gem position og 'start aktivitet direkte' til næste gang
      akt.prefs.edit().
              putString("sidstKlikketPåAkt", aktKlik).
              putInt("kategoriPos", kategoriPos).
              commit();
    }

    public boolean onItemLongClick(AdapterView<?> listView, View v, int position, long id) {
      akt.visKildekode(klasserDerVisesNu.get(position));
      return true;
    }
  }
}

