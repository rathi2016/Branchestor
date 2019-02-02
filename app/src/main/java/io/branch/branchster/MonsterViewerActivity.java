package io.branch.branchster;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


import java.util.Map;

import io.branch.branchster.fragment.InfoFragment;
import io.branch.branchster.util.MonsterImageView;
import io.branch.branchster.util.MonsterObject;
import io.branch.branchster.util.MonsterPreferences;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.BRANCH_STANDARD_EVENT;
import io.branch.referral.util.BranchEvent;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

public class MonsterViewerActivity extends FragmentActivity implements InfoFragment.OnFragmentInteractionListener {
    public static final String MY_MONSTER_OBJ_KEY = "my_monster_obj_key";
    static final int SEND_SMS = 12345;
    private static String TAG = MonsterViewerActivity.class.getSimpleName();
    TextView monsterUrl;
    View progressBar;

    MonsterImageView monsterImageView_;
    MonsterObject myMonsterObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monster_viewer);

        monsterImageView_ = (MonsterImageView) findViewById(R.id.monster_img_view);
        monsterUrl = (TextView) findViewById(R.id.shareUrl);
        progressBar = findViewById(R.id.progress_bar);

        // Change monster
        findViewById(R.id.cmdChange).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MonsterCreatorActivity.class);
                startActivity(i);
                finish();
            }
        });

        // More info
        findViewById(R.id.infoButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                InfoFragment infoFragment = InfoFragment.newInstance();
                ft.replace(R.id.container, infoFragment).addToBackStack("info_container").commit();
            }
        });

        //Share monster
        findViewById(R.id.share_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareMyMonster();
            }
        });

        initUI();

        String savedState = myMonsterObject.monsterMetaData().get(savedInstanceState);

        new BranchEvent("monster_view")
                .setDescription("user visited the monster view page")
                .addCustomDataProperty("State information", savedState)
                .logEvent(MonsterViewerActivity.this);
    }


    private void initUI() {
        myMonsterObject = getIntent().getParcelableExtra(MY_MONSTER_OBJ_KEY);
        progressBar.setVisibility(View.VISIBLE);

        if (myMonsterObject != null) {
            String monsterName = getString(R.string.monster_name);

            // Create an object of LinkProperties to set the channel to be "sms"
            LinkProperties linkProperties = new LinkProperties().setChannel("sms").setFeature("sharing");

            // Create an object of ContentMetadata with the values from `myMonsterObject.prepareBranchDict()`
            ContentMetadata contentMetadata = new ContentMetadata();
            for (Map.Entry<String, String> entry : myMonsterObject.prepareBranchDict().entrySet()) {
                contentMetadata.addCustomMetadata(entry.getKey(), entry.getValue());
            }

            // Create an object of BranchUniversalObject with the ContentMetadata object previously created
            BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                    .setTitle("Branchster Monster: " + myMonsterObject.getMonsterName())
                    .setContentDescription("Monster created and shared by Branch's SDK")
                    .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                    .setContentMetadata(contentMetadata);


            // generate a shortURL and set the value of url text view.
            branchUniversalObject.generateShortUrl(getApplicationContext(), linkProperties, new Branch.BranchLinkCreateListener() {
                @Override
                public void onLinkCreate(String url, BranchError error) {
                    if (error == null) {
                        monsterUrl.setText(url);
                        new BranchEvent("branch_url_created").logEvent(getApplicationContext());
                    } else {
                        new BranchEvent("branch_error").logEvent(getApplicationContext());
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });

            if (!TextUtils.isEmpty(myMonsterObject.getMonsterName())) {
                monsterName = myMonsterObject.getMonsterName();
            }

            ((TextView) findViewById(R.id.txtName)).setText(monsterName);
            String description = MonsterPreferences.getInstance(this).getMonsterDescription();

            if (!TextUtils.isEmpty(myMonsterObject.getMonsterDescription())) {
                description = myMonsterObject.getMonsterDescription();
            }

            ((TextView) findViewById(R.id.txtDescription)).setText(description);

            // set my monster image
            monsterImageView_.setMonster(myMonsterObject);

        } else {
            Log.e(TAG, "Monster is null. Unable to view monster");
            progressBar.setVisibility(View.GONE);

        }
    }

    /**
     * Method to share my custom monster with sharing with Branch Share sheet
     */
    private void shareMyMonster() {
        progressBar.setVisibility(View.VISIBLE);
        /**   //DONE: Replaced with Branch-generated shortUrl */
        String url = "https://gyhk.app.link/uLuTcHa5VT";

        // Create an object of LinkProperties to set the channel to be "sms"

        LinkProperties linkProperties = new LinkProperties()
                .setChannel("sms")
                .setFeature("sharing");

        // Create an object of ContentMetadata with the values from `myMonsterObject.prepareBranchDict()`
        ContentMetadata contentMetadata = new ContentMetadata();
        for (Map.Entry<String, String> entry : myMonsterObject.prepareBranchDict().entrySet()) {
            contentMetadata.addCustomMetadata(entry.getKey(), entry.getValue());
        }

        // Create an object of BranchUniversalObject with the ContentMetadata object previously created

        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setTitle("Branchster Monster: " + myMonsterObject.getMonsterName())
                .setContentDescription("Monster created and shared by Branch's SDK")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(contentMetadata);

        // generate a shortURL
        branchUniversalObject.generateShortUrl(getApplicationContext(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    new BranchEvent("branch_url_created").logEvent(getApplicationContext());
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, String.format("Check out my Branchster named %s at %s", myMonsterObject.getMonsterName(), url));
                    startActivityForResult(i, SEND_SMS);
                } else {
                    new BranchEvent("branch_error").logEvent(getApplicationContext());
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SEND_SMS == requestCode) {
            if (RESULT_OK == resultCode) {
                Log.i("Value of intent ", data.toString());
                /**      //DONE: Track successful share via Branch. */
                final String monsterName = myMonsterObject.getMonsterName();
                new BranchEvent(BRANCH_STANDARD_EVENT.SHARE)
                        .setDescription("Monster share")
                        .addCustomDataProperty("Monster Name", monsterName)
                        .logEvent(this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
        }
    }


    @Override
    public void onFragmentInteraction() {
        //no-op
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initUI();
    }
}
