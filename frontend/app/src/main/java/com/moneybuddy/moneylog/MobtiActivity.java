package com.moneybuddy.moneylog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MobtiActivity extends AppCompatActivity {
    int page = 1;
    static int mobti_result = 0;
    TextView finish, number, question;
    Button answera, answerb;
    ImageView image;
    ProgressBar progressBar;
    Intent mobti_main_page, mobti_result_page;

    public void newPage(int npage) {
        String[] questionvalues = {
                "월급을 받았을 때\n가장 먼저 드는 생각은\n무엇인가요?",
                "당신의 한 달 예산은\n어떻게 관리되나요?",
                "지출 후 소비 기록을\n어떻게 하시나요?",
                "친구들과의 소비 생활에서\n당신은 어떤 스타일인가요?"
        };
        String[] answersa = {
                "얼마나 저축할 수 있을까?",
                "미리 계획해서 예산을 짜놓는다",
                "앱이나 가계부에 자세히 기록한다",
                "개인 소비를 따로 챙기는 편이다"
        };
        String[] answersb = {
                "뭘 사면 좋을까?",
                "그때그때 상황에 맞게 쓴다",
                "기록하지 않거나 대충만 한다",
                "친구와 정보를 나누고\n소비 스타일을 공유한다"
        };
        int[] images = {R.drawable.mobti1, R.drawable.mobti2, R.drawable.mobti3, R.drawable.mobti4};

        int finish_page = npage-1;
        finish.setText("4개의 질문 중 " + finish_page + "개 완료");
        number.setText("Q" + npage);
        question.setText(questionvalues[npage-1]);
        answera.setText(answersa[npage-1]);
        answerb.setText(answersb[npage-1]);
        image.setImageResource(images[npage-1]);
        progressBar.setProgress(npage);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobti);

        finish = findViewById(R.id.textView3);
        number = findViewById(R.id.textView4);
        question = findViewById(R.id.textView5);

        answera = findViewById(R.id.button4);
        answerb = findViewById(R.id.button5);

        image = findViewById(R.id.imageView);

        progressBar = findViewById(R.id.progressBar);

        mobti_main_page  = new Intent(this, MainActivity.class);
        mobti_result_page = new Intent(this, MobtiResultActivity.class);

        newPage(page);
    }

    public void onButton3Clicked(View v) {
        if (page >= 2 && page <= 4) {
            mobti_result %= (int)Math.pow(10, page-1);
            page--;
            newPage(page);
        } else if (page == 1) {
            startActivity(mobti_main_page);
        }
    }

    public void onButton4Clicked(View v) {
        if (page >= 1 && page <= 3) {
            page++;
            newPage(page);
        } else if (page == 4) {
            startActivity(mobti_result_page);
        }
    }

    public void onButton5Clicked(View v) {
        if (page >= 1 && page <= 3) {
            mobti_result += (int)Math.pow(10, page-1);
            page++;
            newPage(page);
        } else if (page == 4) {
            mobti_result += (int)Math.pow(10, page-1);
            startActivity(mobti_result_page);
        }
    }
}
