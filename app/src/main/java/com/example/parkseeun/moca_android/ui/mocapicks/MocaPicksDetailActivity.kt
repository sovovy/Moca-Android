package com.example.parkseeun.moca_android.ui.mocapicks

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ToggleButton
import com.bumptech.glide.Glide
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.model.EvaluationViewItem
import com.example.parkseeun.moca_android.model.delete.DeleteScrapResponse
import com.example.parkseeun.moca_android.model.get.*
import com.example.parkseeun.moca_android.model.post.PostScrapResponse
import com.example.parkseeun.moca_android.network.ApplicationController
import com.example.parkseeun.moca_android.util.ImageAdapter
import com.example.parkseeun.moca_android.util.User
import kotlinx.android.synthetic.main.activity_moca_picks_detail.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MocaPicksDetailActivity : AppCompatActivity() {

    private val TAG = "MocaPicksDetailActivity"

    private var cafe_id : Int  = 0
    private val networkService  = ApplicationController.instance.networkService
    private lateinit var getEvaluatedCafeDetailResponse: Call<GetEvaluatedCafeDetailResponse> // 검증카페 상세 정보 조회
    private lateinit var getEvaluatedCafeImgListResponse: Call<GetEvaluatedCafeImgListResponse> // 검증 카페 이미지 리스트 조회
    private lateinit var getEvaluationListResponse :Call<GetEvaluationListResponse> // 검증 평가 리스트 조회
    private lateinit var postScrapResponse:Call<PostScrapResponse> //스크랩 하기
    private lateinit var deleteScrapResponse:Call<DeleteScrapResponse>// 스크랩 취소 하기



    //view 요소
    private var cafe_name:TextView ?= null //카페 이름
    private var cafe_location:TextView ?= null //카페 위치
    private var total_rating:RatingBar ?= null //카페 별점
    private var summary:TextView ?= null //카페 총평

    private var rb_quality:RatingBar ?= null
    private  var rb_roasting:RatingBar ?= null
    private var rb_creativity:RatingBar ?= null
    private  var rb_reasonable:RatingBar ?= null
    private  var rb_consistancy:RatingBar ?= null

    private lateinit var rl_baristaContainer1:RelativeLayout
    private lateinit var rl_baristaContainer2:RelativeLayout
    private lateinit var rl_baristaContainer3:RelativeLayout

    lateinit var tb_scrab:ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_moca_picks_detail)
        var scrab_is: Boolean = false
        var intent:Intent  =  getIntent()

        cafe_id = intent.getIntExtra("cafe_id",-1)
        scrab_is = intent.getBooleanExtra("scrab_is",false)
        Log.v("cafe_id", ""+cafe_id)
        Log.v("scrab_is",scrab_is.toString())
        cafe_name = findViewById(R.id.tv_mocaPicksDetail_cafeName)
        cafe_location = findViewById(R.id.tv_mocaPicksDetail_cafeLocation)
        total_rating = findViewById(R.id.rating_mocaPicksList_total)
        summary = findViewById(R.id.tv_mocaPicks_summary)

        rb_quality = findViewById(R.id.rating_mocaPicksList_item1)

        rb_roasting = findViewById(R.id.rating_mocaPicksList_item2)

        rb_creativity = findViewById(R.id.rating_mocaPicksList_item3)

        rb_reasonable = findViewById(R.id.rating_mocaPicksList_item4)

        rb_consistancy = findViewById(R.id.rating_mocaPicksList_item5)


        rl_baristaContainer1 = findViewById(R.id.baristaContainer1)
        rl_baristaContainer2 = findViewById(R.id.baristaContainer2)
        rl_baristaContainer3 = findViewById(R.id.baristaContainer3)




        tb_scrab = findViewById(R.id.tb_mocaPicksScrab)
        tb_scrab.isChecked = scrab_is
        tb_scrab.setOnClickListener {
            if(tb_scrab.isChecked)
            {
                PostScrap()


            }
            else
            {
                DeleteScrap()
            }
        }
       // var urlList: Array<String?> = arrayOf("http://img.hani.co.kr/imgdb/resize/2017/1222/151381249807_20171222.JPG","http://img.hani.co.kr/imgdb/resize/2017/1222/151381249807_20171222.JPG", "http://img.hani.co.kr/imgdb/resize/2017/1222/151381249807_20171222.JPG", "http://img.hani.co.kr/imgdb/resize/2017/1222/151381249807_20171222.JPG", "http://img.hani.co.kr/imgdb/resize/2017/1222/151381249807_20171222.JPG")

        getEvaluatedCafeDetail()
       // progressBar_mocaPicksDetail.max = urlList.size
        getEvaluatedCafeImageList()
        getEvaluationList()

        vp_mocaPicksDetail.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(p0: Int) { // 이미지 넘길 떄 하는 일은 여기서,,
                progressBar_mocaPicksDetail.progress = p0 + 1
            }
        })


        // 평가보기 버튼 리스너
        ib_baristaEvaluate1.setOnClickListener {
            val intent = Intent(this@MocaPicksDetailActivity, MocaPicksBaristaActivity::class.java)

            startActivity(intent)
        }

        ib_baristaEvaluate2.setOnClickListener {
            val intent = Intent(this@MocaPicksDetailActivity, MocaPicksBaristaActivity::class.java)

            startActivity(intent)
        }

        ib_baristaEvaluate3.setOnClickListener {
            val intent = Intent(this@MocaPicksDetailActivity, MocaPicksBaristaActivity::class.java)

            startActivity(intent)
        }

        // 뒤로가기 버튼
        ib_moca_picks_detail_back.setOnClickListener {
            finish()
        }
    }



    /**
     * 검증 카페 상세 정보 조회 통신
     */
    private fun getEvaluatedCafeDetail() {
        getEvaluatedCafeDetailResponse  = networkService.getEvaluatedCafeDetail(User.token!!, cafe_id!!)
        getEvaluatedCafeDetailResponse.enqueue(object: Callback<GetEvaluatedCafeDetailResponse> {
            override fun onFailure(call: Call<GetEvaluatedCafeDetailResponse>, t: Throwable) {
                toast(t.toString())
            }

            override fun onResponse(call: Call<GetEvaluatedCafeDetailResponse>, response: Response<GetEvaluatedCafeDetailResponse>) {
                if(response.isSuccessful) {
                    if(response.body()!!.status==200) {
                        cafe_name!!.text = response.body()!!.data.cafe_name
                        cafe_location!!.text = response.body()!!.data.cafe_address_detail
                        total_rating!!.rating = response.body()!!.data.evaluated_cafe_rating.toFloat()
                        summary!!.text = response.body()!!.data.evaluated_cafe_total_evaluation

                    }
                    else {
                        toast(response.body()!!.status.toString() + " : " + response.body()!!.message)
                    }
                }
            }
        })
    }

    /**
     * 검증카페 이미지 정보 조회
     *
     *
     */
    private fun getEvaluatedCafeImageList()
    {
        getEvaluatedCafeImgListResponse = networkService.getEvaluatedCafeImageList(User.token!!, cafe_id!!)
        getEvaluatedCafeImgListResponse.enqueue(object: Callback<GetEvaluatedCafeImgListResponse>{

            override fun onFailure(call: Call<GetEvaluatedCafeImgListResponse>, t: Throwable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


            }

            override fun onResponse(call: Call<GetEvaluatedCafeImgListResponse>, response: Response<GetEvaluatedCafeImgListResponse>) {
                if(response.isSuccessful) {
                    if(response.body()!!.status==200) {


                        var urlList: ArrayList<EvaluatedCafeImg> = response.body()!!.data
                        var imgList = arrayOfNulls<String>(urlList.size)
                        for(i in 0..(urlList.size-1)){
                            imgList[i] = urlList.get(i).evaluated_cafe_img_url
                        }
                        progressBar_mocaPicksDetail.max = imgList.size
                        vp_mocaPicksDetail.adapter = ImageAdapter(applicationContext, imgList)
                    }
                    else {
                        toast(response.body()!!.status.toString() + " : " + response.body()!!.message)
                    }
                }

            }
        })



    }


    /**
     * 검증카페 인증 위원 평가 리스트
     */

    private  fun getEvaluationList()
    {
        getEvaluationListResponse = networkService.getEvaluationList(User.token!!, cafe_id!!)
        getEvaluationListResponse.enqueue(object: Callback<GetEvaluationListResponse>{

            override fun onFailure(call: Call<GetEvaluationListResponse>, t: Throwable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


            }
            override fun onResponse(call: Call<GetEvaluationListResponse>, response: Response<GetEvaluationListResponse>) {
                if(response.isSuccessful) {
                    if(response.body()!!.status==200) {
                        var evaluationList: ArrayList<Evaluation> = response.body()!!.data
                        var evaluationViewItems : ArrayList<EvaluationViewItem> =  ArrayList()
                        val barista_img = R.id.civ_baristaImage1
                        val barista_name = R.id.tv_baristaName1
                        val barista_title = R.id.tv_baristaSpec1

                        /****************************/
                        var quality_of_bean:Int = 0 ;
                        var roasting_skill:Int = 0;
                        var creativity:Int = 0;
                        var reasonable_price:Int = 0;
                        var consistant_taste:Int =0;
                        /****************************/
                        for(i in 0..(evaluationList.size-1))
                        {
                            var evaluationViewItem:EvaluationViewItem =  EvaluationViewItem(findViewById(barista_img+i),findViewById(barista_name+i),findViewById(barista_title+i))
                            evaluationViewItems.add(evaluationViewItem)

                            evaluationViewItems.get(i).baristaName!!.text = evaluationList.get(i).barista_title
                            evaluationViewItems.get(i).baristaTitle!!.text = evaluationList.get(i).barista_name
                            Glide.with(applicationContext).load(evaluationList.get(i).barista_img_url).into(evaluationViewItems.get(i).baristaImage)

                            /**** 평가 평균 *******/
                            quality_of_bean += evaluationList.get(i).evaluation_bean_condition
                            roasting_skill += evaluationList.get(i).evaluation_roasting
                            creativity += evaluationList.get(i).evaluation_creativity
                            reasonable_price += evaluationList.get(i).evaluation_reasonable
                            consistant_taste += evaluationList.get(i).evaluation_consistancy
                        }
                        if(evaluationList.size == 1)
                        {
                            baristaContainer2.visibility = View.INVISIBLE
                            baristaContainer3.visibility = View.INVISIBLE

                        }
                        else if(evaluationList.size == 2)
                        {
                            baristaContainer3.visibility = View.INVISIBLE
                        }





                        if(evaluationList.size >0) {


                            quality_of_bean /= evaluationList.size
                            roasting_skill /= evaluationList.size
                            creativity /= evaluationList.size
                            reasonable_price /= evaluationList.size
                            consistant_taste /= evaluationList.size




                            rb_quality!!.rating = quality_of_bean.toFloat()
                            rb_roasting!!.rating = roasting_skill.toFloat()
                            rb_creativity!!.rating = creativity.toFloat()
                            rb_reasonable!!.rating = reasonable_price.toFloat()
                            rb_consistancy!!.rating = consistant_taste.toFloat()
                        }

                        rl_baristaContainer1.setOnClickListener {
                            val intent : Intent = Intent(applicationContext, MocaPicksBaristaActivity::class.java)
                            intent.putExtra("cafe_id",cafe_id)
                            intent.putExtra("barista_id",evaluationList.get(0).barista_id)

                            startActivity(intent)
                        }
                        rl_baristaContainer2.setOnClickListener {
                            val intent : Intent = Intent(applicationContext, MocaPicksBaristaActivity::class.java)
                            intent.putExtra("cafe_id",cafe_id)
                            intent.putExtra("barista_id",evaluationList.get(1).barista_id)

                            startActivity(intent)
                        }
                        rl_baristaContainer3.setOnClickListener {
                            val intent : Intent = Intent(applicationContext, MocaPicksBaristaActivity::class.java)
                            intent.putExtra("cafe_id",cafe_id)
                            intent.putExtra("barista_id",evaluationList.get(2).barista_id)

                            startActivity(intent)
                        }







                    }
                    else {
                        toast(response.body()!!.status.toString() + " : " + response.body()!!.message)
                    }
                }

            }
        })

    }

    /**
     *스크랩 하기
     *
     */
    private  fun PostScrap()
    {
    postScrapResponse = networkService.postScrap(User.token!!, cafe_id!!)
        postScrapResponse.enqueue(object: Callback<PostScrapResponse>{

            override fun onFailure(call: Call<PostScrapResponse>, t: Throwable?) {
               Log.e(TAG, "post scrab load failed")
            }

            override fun onResponse(call: Call<PostScrapResponse>, response: Response<PostScrapResponse>) {
                if(response.isSuccessful) {
                    if(response.body()!!.status==200) {
                        Log.d("tb" ,"true")
                        tb_scrab.isChecked = true
                    }
                    else {
                        toast(response.body()!!.status.toString() + " : " + response.body()!!.message)
                    }
                }

            }
        })



    }

    /**
     * 스크랩 취소
     *
     */
    private  fun DeleteScrap()
    {
        deleteScrapResponse = networkService.deleteScrap(User.token!!, cafe_id!!)
        deleteScrapResponse.enqueue(object: Callback<DeleteScrapResponse>{

                    override fun onFailure(call: Call<DeleteScrapResponse>, t: Throwable?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.




                    }

                    override fun onResponse(call: Call<DeleteScrapResponse>, response: Response<DeleteScrapResponse>) {
                        if(response.isSuccessful) {
                            if(response.body()!!.status==200) {
                                Log.d("tb" ,"false")
                                tb_scrab.isChecked = false
                            }
                            else {
                                toast(response.body()!!.status.toString() + " : " + response.body()!!.message)
                            }
                        }

                    }
                })

    }






}