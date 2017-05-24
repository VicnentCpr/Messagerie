package com.charpentier.vincent.messagerie;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    private static final int SIGN_IN_REQUEST_CODE = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);


        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Démarage de l'inscription/connection
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            // L'utilisateur est déjà connecté on lui
            // adresse un message de bienvenue.

            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();

            // chargement des messages
            displayChatMessages();
            //annonce ax autres utilisateur que
            //l'utilisateur est connecté
            infoco();

            Button fab =
                    (Button) findViewById(R.id.fab);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    EditText input = (EditText)findViewById(R.id.input);
                    //envoie du message
                    envoie(input);
                    ListView lv = (ListView)findViewById(R.id.list_of_messages);
                    lv.setSelection(lv.getAdapter().getCount()-1);



                }
            });
        }
    }
    public void envoie(EditText v){
        System.out.println("test de passage");
        System.out.println("message :"+v.getText().toString());
        //lecture de l'input et push d'une nouvelle instance
        // de ChatMessage dans la bd de firebase
        String messagetext=v.getText().toString();
        String messageuser=FirebaseAuth.getInstance()
                .getCurrentUser()
                .getDisplayName();
        FirebaseDatabase.getInstance()
                .getReference()
                .push()
                .setValue(new ChatMessage(messagetext,
                        messageuser)
                );
        System.out.println("message est censé être envoyé");

        // nettoyage de l'input
        v.setText("");
    }
    public void infoco(){
        System.out.println("test de passage");
        //lecture de l'input et push d'une nouvelle instance
        // de ChatMessage dans la bd de firebase
        String messageuser=FirebaseAuth.getInstance()
                .getCurrentUser()
                .getDisplayName();
        FirebaseDatabase.getInstance()
                .getReference()
                .push()
                .setValue(new ChatMessage(messageuser+" viens juste de se connecter.",
                        "Information")
                );
        System.out.println("message est censé être envoyé");

    }
    public void infodeco(){
        System.out.println("test de passage");

        //lecture de l'input et push d'une nouvelle instance
        // de ChatMessage dans la bd de firebase

        String messageuser=FirebaseAuth.getInstance()
                .getCurrentUser()
                .getDisplayName();
        FirebaseDatabase.getInstance()
                .getReference()
                .push()
                .setValue(new ChatMessage(messageuser+" viens juste de se deconnecter.",
                        "Information")
                );
        System.out.println("message est censé être envoyé");


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                //la conncetion à fonctionnée
                Toast.makeText(this,
                        "Connection réussie. Bienvenue!",
                        Toast.LENGTH_LONG)
                        .show();
                displayChatMessages();
            } else {
                //la connection ) échouée
                Toast.makeText(this,
                        "Nous ne pouvons pas vous connecter pour le moement. Attendez un peut et réessayez s'il vous plait..",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);


                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());


                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            //deconnection
            infodeco();
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //deconnection réussie et terminée
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Fermeture de l'activity
                            finish();
                        }
                    });
        }
        return true;
    }
}
