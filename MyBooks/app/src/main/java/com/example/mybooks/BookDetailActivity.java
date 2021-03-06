package com.example.mybooks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mybooks.databinding.ActivityBookDetailBinding;
import com.example.mybooks.interfaces.OnLikeBtnClicked;
import com.example.mybooks.models.Book;
import com.example.mybooks.retrofit.BookHttpService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends AppCompatActivity implements OnLikeBtnClicked {

    private ScaleAnimation scaleAnimation;
    //애니메이션이 일어나는 동안의 회수, 속도를 조절하거나 시작과 종료시의 효과를 추가 할 수 있다
    private BounceInterpolator bounceInterpolator;
    private CompoundButton likeButton; // 찜 버튼
    private Button purchaseBtn; // 구매하기 버튼
    private ImageView bookImage; // 책이미지
    private TextView bookTitle; // 책제목
    private TextView author; // 작가
    private TextView publishDate; // 출판일
    private TextView summaryText; // 줄거리 내용

    private Book book;
    public static final String PARAM_NAME_1 = "book obj";
    private ActivityBookDetailBinding binding;
    private BookHttpService bookHttpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bookHttpService = BookHttpService.retrofit.create(BookHttpService.class);
        if (getIntent() != null) {
            book = (Book) getIntent().getSerializableExtra(PARAM_NAME_1);
            initData();

            clickLikeBtn(binding.likeButton);
        }

        for (int i = 0; i < MainActivity.likeBookList.size(); i++){
            if (MainActivity.likeBookList.get(i).getImageUrl().equals(book.getImageUrl())){
                binding.likeButton.setChecked(true);
            }
        }


    }

    public void onPurchaseBtnClicked(View view) { // 구매버튼 클릭 메소드
        // 여기에 구매 URL
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getBuyUrl()));
        startActivity(intent);
    }

    private void initData() {
        Glide.with(this)
                .load(book.getImageUrl()).fitCenter()
                .into(binding.bookImage);

        binding.bookTitle.setText(book.getTitle());
        binding.author.setText(book.getAuthor());
        binding.publishDate.setText(book.getPublicationDate());
        binding.summaryText.setText(book.getIntro());
//        binding.likeButton.setOnClickListener(v -> {
//            SharedPreferences sp = getSharedPreferences("sp", MODE_PRIVATE);
//            boolean isFavorite;
//            if (book.isFavorite() == false) {
//                isFavorite = sp.getBoolean("isFavorite", false);
//            } else {
//                isFavorite = sp.getBoolean("isFavorite", true);
//            }
//            Log.d("TAG", "onDestroy isFavorite : " + isFavorite);
//        });

        Log.d("TAG", String.valueOf(book.isFavorite()));

        // 장르
        switch (book.getTheme()) {
            case 1:
                binding.genre.setText("소설");
                break;
            case 2:
                binding.genre.setText("추리");
                break;
            case 3:
                binding.genre.setText("에쎄이");
                break;
            case 4:
                binding.genre.setText("자기계발");
                break;
            case 5:
                binding.genre.setText("경제");
                break;
            case 6:
                binding.genre.setText("기타");
                break;
            case 7:
                binding.genre.setText("어린이");
                break;
        }

        // 출판사
        binding.publishCompany.setText(book.getPublisher());
        // 가격
        binding.bookPrice.setText(String.valueOf(book.getPrice()));
        // 별점
        binding.ratingBar.setRating((float) book.getRating());

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void clickLikeBtn(View view) {
        binding.likeButton.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("sp", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(String.valueOf(binding.likeButton.isChecked()), true);
            editor.apply();

            if (MainActivity.likeBookList.size() == 0) {
                MainActivity.likeBookList.add(book);
            }
            for (int i = 0; i < MainActivity.likeBookList.size(); i++) {
                if (!MainActivity.likeBookList.get(i).getImageUrl().equals(book.getImageUrl())) {
                    MainActivity.likeBookList.add(book);
                }
            }

            bookHttpService.clickFavorite(book).enqueue(new Callback<Book>() {
                @Override
                public void onResponse(Call<Book> call, Response<Book> response) {
                    if (response.isSuccessful()) {
                        Log.d("TAG", "응답성공");
                    }
                }

                @Override
                public void onFailure(Call<Book> call, Throwable t) {
                    Log.d("TAG", "통신실패");
                }
            });

        });
    }
}