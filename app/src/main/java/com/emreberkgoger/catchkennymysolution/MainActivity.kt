package com.emreberkgoger.catchkennymysolution
import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.emreberkgoger.catchkennymysolution.databinding.ActivityMainBinding
import java.util.Random
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityMainBinding
    private var score = 0
    var kennyImages = ArrayList<ImageView>()
    var kyleImages = ArrayList<ImageView>()
    var currentImages = ArrayList<ImageView>()
    var random = Random()
    private lateinit var sharedPrefMaxScore : SharedPreferences     // Anahtar kilit şeklinde veri tutmamızı sağlar. Burada da max score bilgisini saklamak için kullanacağız.
    private lateinit var sharedPrefGameSpeed : SharedPreferences    // gameCount değişkenine göre oyunun hızını ayarlamamızı sağlayan sharedPreferences yapısı.
    private lateinit var sharedPrefCurrentScore : SharedPreferences  // Oyunda level ilerledikçe skor toplanarak gideceği için skoru diğer ekrana taşımamızı sağlayan yapı.
    var maxScore :Int = 0
    var gameCount = 1
    var level = 1

    @SuppressLint("SetTextI18n", "CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        // view binding işlemleri:
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // image seçilirek kullanıcının istediği görselle oyun oynanıyor.
        chooseImage("kenny")


        // Yukarıda tanımlanan ve amaçları verilen sharedPreferences yapılarının tanımlanması.
        sharedPrefMaxScore = this.getSharedPreferences("com.emreberkgoger.CatchKennyMySolution", MODE_PRIVATE)
        sharedPrefGameSpeed = this.getSharedPreferences("com.emreberkgoger.CatchKennyMySolution", MODE_PRIVATE)
        sharedPrefCurrentScore = this.getSharedPreferences("com.emreberkgoger.CatchKennyMySolution", MODE_PRIVATE)

        maxScore = sharedPrefMaxScore.getInt("maxScore", 0)  // max skoru tutan değişken, değer gelmezse default 0 olacak.
        gameCount = sharedPrefGameSpeed.getInt("gameCount", 1)  // Default değer 1
        score = sharedPrefCurrentScore.getInt("current", 0)

        // Oyun kapanıp açıldığında problem oluşabiliyordu. Buna karşın leveli kontrol edip skor ve gameCountu başlangıç değerlerine atadık.
        if (level == 1)
        {
            sharedPrefGameSpeed.edit().putInt("gameCount", 1).apply()
            sharedPrefCurrentScore.edit().putInt("current", 0).apply()
        }

        binding.maxScoreText.text = "Max Score: ${maxScore}"  // Max skoru yazdırıyoruz.



        // Geri sayma işlemini yapacağımız abstract class
        // Implemente etmek zorunda olduğumuz iki tane metodu var: onTick() ve onFinish()
        object : CountDownTimer(15500, ((1000/gameCount)*2).toLong())   // İkinci parametre, sharedPreferences ve intent sayesinde oyun hızını her restrartta hızlandırabiliyoruz.
        {
            @SuppressLint("SetTextI18n")
            // Her bir saniyede ne yapılacağını belirleyen metot.
            override fun onTick(p0: Long)
            {
                hideImage(currentImages)
                binding.textView.setText("Left: ${p0 / 1000} seconds")
            }


            val alert = AlertDialog.Builder(this@MainActivity)
            val endOfGameAlert = AlertDialog.Builder(this@MainActivity)
            @SuppressLint("SetTextI18n")
            // Oyun bittiğinde ne yapılacağını yazdığımız metot
            override fun onFinish()
            {
                // Oyun sonunda ekranda görsel kalmasın diye görünen görsel de invisible yapılır.
                // Aksi takdirde alertDialog yapısı kapatılıp görsele tıklayarak skor arttırılmaya devam ettirilebilir.
                for (image in currentImages)
                {
                    image.visibility = View.INVISIBLE
                }
                level += 1

                //***************************************************************************************************************
                // Oyun 10 levelden olacak şekilde kurgulandı o yüzden oyun sayısı 10a kadar geldi mi diye kontrol yapıyoruz.
                if (gameCount > 10)
                {
                    endOfGameAlert.setTitle("You've reached the end of game!")
                    endOfGameAlert.setMessage("Here is your score: ${score}")
                    endOfGameAlert.setNeutralButton("OKEY"){dialog, which ->
                        finish()
                    }
                    endOfGameAlert.show()
                }

                else
                {
                    alert.setTitle("${gameCount}. level is Over")
                    alert.setMessage("Next level ?")
                    // intent yapısı ekranlar arasındaki bağı kontrol eden bir yapı. Burada da tekrar aynı ekrana döneceğimiz için kullanıyoruz.

                    // intent fonksiyonu çağırılarak sabitimize atanır.
                    val intent = intent

                    alert.setPositiveButton("Yes") { dialog, which ->
                        // Bir sonraki levele geçecek kod.
                        // Oyuncu bir sonraki levele geçmek istediğinde skorunu ve arttırılmış oyun hızını yeni ekranda aktarmak için ilgili sharedPreferences yapıları güncellenip intent içinde bir sonraki ekrana aktarılır.
                        gameCount += 1
                        sharedPrefGameSpeed.edit().putInt("gameCount", gameCount).apply() // SharedPreferences yapısıyla gameCount değişkenini arttırıyoruz.
                        sharedPrefCurrentScore.edit().putInt("current", score).apply()
                        intent.putExtra("gameCount", sharedPrefGameSpeed.getInt("gameCount", gameCount))   // intent'e sharedPreferences yapısını uyguluyoruz ki diğer ekranda istedğimiz şeyi elde edelim.
                        intent.putExtra("current", sharedPrefCurrentScore.getInt("current", score))
                        finish()    // Önceki uygulamayı bitirip sadece yeni ekranın çalışmasını sağlar.
                        startActivity(intent)
                    }

                    alert.setNegativeButton("No") {dialog, which ->
                        finish()
                    }
                    // Max skoru güncelleyecek işlemleri butonların altında yapıyoruz çünkü her iki durumda da max skorun güncellenmesini istiyoruz.
                    updateMaxScore()        // Max skoru oyun bittikten sonra update ediyoruz.
                    intent.putExtra("maxScore", sharedPrefMaxScore.getInt("maxScore", 0))       // Sonraki ekranda olması için intente putExtra ile maxScoru veriyoruz.

                    alert.show()
                }
            }
        }.start()
    }


    @SuppressLint("SetTextI18n")
    fun increaseScore(view : View)
    {
        score += (gameCount)
        binding.textView2.setText("Score: $score")

    }

    fun updateMaxScore()
    {
        if (score > maxScore)
            maxScore = score
        sharedPrefMaxScore.edit().putInt("maxScore", maxScore).apply()
    }


    // Görsellerden sadece birini görünür yapıp diğerlerini görünmez bırakan metot.
    fun hideImage(images : ArrayList<ImageView> )
    {
        for (image in images)
        {
            // Bütün imageları görünmez yapar.
            image.visibility = View.INVISIBLE
        }
        val index = rand(1,9)
        images[index].visibility = View.VISIBLE
    }

    // Rastgele integer veren metot. HideImage() içinde kullanıyoruz.
    fun rand(from: Int, to: Int) : Int
    {
        return random.nextInt(to - from) + from
    }

    // 2 tane image array yaratarak gerekli görselleri id'leri aracılığıyla arraya ekliyoruz.
    fun createImageArray()
    {
        kennyImages.add(binding.kennyImage1)
        kennyImages.add(binding.kennyImage2)
        kennyImages.add(binding.kennyImage3)
        kennyImages.add(binding.kennyImage4)
        kennyImages.add(binding.kennyImage5)
        kennyImages.add(binding.kennyImage6)
        kennyImages.add(binding.kennyImage7)
        kennyImages.add(binding.kennyImage8)
        kennyImages.add(binding.kennyImage9)

        kyleImages.add(binding.kyleImage1)
        kyleImages.add(binding.kyleImage2)
        kyleImages.add(binding.kyleImage3)
        kyleImages.add(binding.kyleImage4)
        kyleImages.add(binding.kyleImage5)
        kyleImages.add(binding.kyleImage6)
        kyleImages.add(binding.kyleImage7)
        kyleImages.add(binding.kyleImage8)
        kyleImages.add(binding.kyleImage9)

    }
    // Kullanılmayacak olan arrayin görsellerini gizleyen metot.
    fun makeInvisible(imageArray : ArrayList<ImageView>)
    {
        for (image in imageArray)
        {
            image.visibility = View.INVISIBLE
        }
    }

    // Verilen parametreye göre görsel seçen metot.
    fun chooseImage(name : String)
    {
        createImageArray()

        if (name.equals("kyle"))
        {
            makeInvisible(kennyImages)
            currentImages = kyleImages
        }

        else if(name.equals("kenny"))
        {
            makeInvisible(kyleImages)
            currentImages = kennyImages
        }
    }

}


