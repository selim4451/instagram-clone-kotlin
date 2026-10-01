package com.example.instagramclone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.instagramclone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Alt boşluğu vermiyoruz: BottomNavigationView gezinme çubuğu boşluğunu kendisi ekliyor.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.navExplore -> ExploreFragment()
                R.id.navCreate -> CreateFragment()
                R.id.navReels -> ReelsFragment()
                R.id.navProfile -> ProfileFragment()
                else -> HomeFragment() // navHome
            }
            showFragment(fragment)
            true
        }

        // Zaten açık olan sekmeye tekrar basılınca Fragment'ı baştan oluşturma.
        binding.bottomNav.setOnItemReselectedListener { }

        // Ekran döndürmede Android Fragment'ı kendisi geri yükler; sadece ilk açılışta ekliyoruz.
        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
