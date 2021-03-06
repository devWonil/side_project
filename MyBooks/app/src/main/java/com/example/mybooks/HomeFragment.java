package com.example.mybooks;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mybooks.adapter.RandomBookAdapter;
import com.example.mybooks.adapter.SliderImageAdapter;
import com.example.mybooks.interfaces.OnBookItemClicked;

import com.example.mybooks.models.Book;

import com.example.mybooks.retrofit.BookHttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnBookItemClicked {

    private ViewGroup viewGroup;
    private LinearLayout layoutIndicatorsContainer;
    private static HomeFragment fragment;
    private ArrayList<String> imageURL = new ArrayList<>();
    private int currentPage;
    Timer timer;

    // RecyclerView
//    private FragmentHomeBinding binding;
    private RecyclerView randomBookContainer;
    private BookHttpService bookHttpService;
    private RandomBookAdapter randomBookAdapter;
    private List<Book> bookList = new ArrayList<>();
    // 스크롤시 중복 발생
    private boolean isFirstLoading = true;

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        if (fragment == null) {
            fragment = new HomeFragment();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 샘플이미지 넣어주기
        imageURL.add("http://image.kyobobook.co.kr/ink/images/prom/2022/book/220509_bellygom/bn/bnE_w01_c3c5f7.jpg");
        imageURL.add("http://image.kyobobook.co.kr/newimages/adcenter/IMAC/creatives/2022/05/30/50520/20220530-1.jpg");
        imageURL.add("http://image.kyobobook.co.kr/ink/images/prom/2022/book/220607_bestseller/bn/bnG_w01_a8daff.jpg");
        imageURL.add("http://image.kyobobook.co.kr/newimages/adcenter/IMAC/creatives/2022/06/03/61174/kyobo_newbook.jpg");
        imageURL.add("http://image.kyobobook.co.kr/newimages/adcenter/IMAC/creatives/2022/06/03/57753/EG_newbook.jpg");
        bookHttpService = BookHttpService.retrofit.create(BookHttpService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);
        initData();
        setRandomRecyclerView(bookList);
        requestRandomBookData();
        return viewGroup;
    }

    // SliderImageView 설정
    private void initData() {
        ViewPager2 viewPager2 = viewGroup.findViewById(R.id.slideViewPager);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setAdapter(new SliderImageAdapter(getActivity(), imageURL));
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
                currentPage = position;
            }
        });
        setIndicators(imageURL.size());
        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                if(currentPage == 5) {
                    currentPage = 0;
                }
                viewPager2.setCurrentItem(currentPage++, true);
            }
        };
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 400, 3000);
    }

    //     indicator 띄우기
    private void setIndicators(int count) {
        layoutIndicatorsContainer = viewGroup.findViewById(R.id.indicatorContainer);
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 5, 10, 5);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getActivity());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_indicator_active));
            indicators[i].setLayoutParams(params);

            layoutIndicatorsContainer.addView(indicators[i]);
        }
    }

    // 현재 띄우고 있는 indicator 연결
    private void setCurrentIndicator(int position) {
        int count = layoutIndicatorsContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            ImageView imageView = (ImageView) layoutIndicatorsContainer.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_indicator_active));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.bg_indicator_inactive));
            }
        }
    }

    // recyclerView 통신
    private void requestRandomBookData() {
        bookHttpService.getRandomList().enqueue(new Callback<ArrayList<Book>>() {
            @Override
            public void onResponse(Call<ArrayList<Book>> call, Response<ArrayList<Book>> response) {
                if(response.isSuccessful()) {
                    List<Book> bookList = response.body();
                    randomBookAdapter.initBookList(bookList);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Book>> call, Throwable t) {
                Log.d("TAG", t.getMessage() + "통신 실패");
            }
        });
    }

    //
    private void setRandomRecyclerView(List<Book> bookList) {
        randomBookContainer = viewGroup.findViewById(R.id.randomBookContainer);
        randomBookAdapter = new RandomBookAdapter();
        randomBookAdapter.setOnBookItemClicked(this);
        randomBookAdapter.initBookList(bookList);
        randomBookContainer.setAdapter(randomBookAdapter);
        randomBookContainer.hasFixedSize();
    }


    @Override
    public void selectItem(Book book) {
        Intent intent = new Intent(getContext(), BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.PARAM_NAME_1, book);
        startActivity(intent);
    }
}