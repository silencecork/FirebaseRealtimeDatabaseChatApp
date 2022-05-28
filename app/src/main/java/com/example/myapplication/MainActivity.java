package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private ChatListAdapter mAdapter;
    private EditText mInputMessage;
    private Button mSendButton;
    private DatabaseReference mRef;
    private String mUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "start app");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText nameInput = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Please enter your name")
                .setView(nameInput)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mUserName = nameInput.getEditableText().toString();
                        if (!TextUtils.isEmpty(mUserName)) {
                            getAllMessages();
                        }
                    }
                })
                .show();

        mInputMessage = findViewById(R.id.input_message);
        mSendButton = findViewById(R.id.send_button);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRef = database.getReference("messages");

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mInputMessage.getEditableText().toString();
                if (TextUtils.isEmpty(message)) {
                    return;
                }

                MessageModel messageModel = new MessageModel(message, mUserName);
                sendMessageToFirebase(messageModel);

                mInputMessage.setText("");
            }
        });

        mRecyclerView = findViewById(R.id.chat_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ChatListAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void getAllMessages() {
        mRef.orderByChild("messageTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                if (!snapshot.hasChildren()) {
                    return;
                }
                List<MessageModel> list = new ArrayList<>();
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    DataSnapshot dataSnapshot = iterator.next();
                    MessageModel model = dataSnapshot.getValue(MessageModel.class);
                    list.add(model);
                }
                mAdapter.addMessages(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startGetOneTimeData() {
        mRef.orderByChild("messageTime").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "all value event");
                if (snapshot.exists()) {
                    if (snapshot.hasChildren()) {
                        Log.d(TAG, "children count " + snapshot.getChildrenCount());
                        Iterable<DataSnapshot> iterable = snapshot.getChildren();
                        Iterator<DataSnapshot> iterator = iterable.iterator();
                        while (iterator.hasNext()) {
                            DataSnapshot dataSnapshot = iterator.next();
                            MessageModel model = dataSnapshot.getValue(MessageModel.class);
                            mAdapter.addMessage(model);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void sendMessageToFirebase(MessageModel model) {
        mRef.child("" + model.hashCode()).setValue(model);
    }


    class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context mContext;
        private List<MessageModel> mChatList = new ArrayList<>();

        public ChatListAdapter(Context context) {
            this.mContext = context;
        }

        public void addMessages(List<MessageModel> models) {
            mChatList.clear();
            mChatList.addAll(models);
            notifyDataSetChanged();
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            }, 100);
        }

        public void addMessage(MessageModel model) {
            if (mChatList.contains(model)) {
                return;
            }
            mChatList.add(model);
            notifyDataSetChanged();

            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            }, 100);
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {

            TextView mChatContent;
            TextView mTime;
            TextView mName;

            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.text_message_name);
                mTime = itemView.findViewById(R.id.text_message_time);
                mChatContent = itemView.findViewById(R.id.text_message_incoming);
            }

            void bind(int position) {
                MessageModel messageModel = mChatList.get(position);
                mName.setText(messageModel.userName);
                mTime.setText(messageModel.messageTime);
                mChatContent.setText(messageModel.chatContent);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View chatBubble = LayoutInflater.from(mContext).inflate(R.layout.chat_bubble, parent, false);
            return new MessageViewHolder(chatBubble);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder viewHoder = (MessageViewHolder) holder;
            viewHoder.bind(position);
        }

        @Override
        public int getItemCount() {
            return mChatList.size();
        }
    }
}