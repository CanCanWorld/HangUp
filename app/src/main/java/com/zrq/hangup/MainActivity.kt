package com.zrq.hangup

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.tencent.mmkv.MMKV
import com.zrq.hangup.adapter.LogsAdapter
import com.zrq.hangup.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mmkv: MMKV
    private var level = 0L
    private var expTotal = 0L
    private var expCurrent = 0L
    private var expRatio = 99 * 99
    private var exitTime = 0L
    private var speed = 2000L
    private val logs = mutableListOf<String>()
    private lateinit var adapter: LogsAdapter
    private var enemyHpTotal = 100L
    private var enemyHpCurrent = 100L
    private var enemyHpRatio = 99 * 9
    private var attack = 1L
    private var attackGetRadio = 1L
    private var expGetRadio = 1L
    private var speedGetRadio = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        MMKV.initialize(this)

        initData()
        initEvent()

    }

    private fun initData() {
        mmkv = MMKV.defaultMMKV()
        adapter = LogsAdapter(this, logs)
        mBinding.recyclerView.adapter = adapter
        level = mmkv.getLong("level", 0)
        expCurrent = mmkv.getLong("exp", 0)
        exitTime = mmkv.getLong("exitTime", 0)
        attack = mmkv.getLong("attack", 1)
        speed = mmkv.getLong("speed", 2000)
        attackGetRadio = mmkv.getLong("attackGetRadio", 1)
        expGetRadio = mmkv.getLong("expGetRadio", 1)
        speedGetRadio = mmkv.getLong("speedGetRadio", 1)
        enemyHpCurrent = level * enemyHpRatio
        enemyHpTotal = level * enemyHpRatio
        expTotal = level * expRatio
        Log.d(TAG, "level: $level")
        Log.d(TAG, "expCurrent: $expCurrent")
        Log.d(TAG, "exitTime: $exitTime")
        val nowTime = Date().time
        val ackCount = (nowTime - exitTime) / speed
        Log.d(TAG, "ackCount: $ackCount")
        refreshUi()
        val myTimerTask = MyTimerTask()
        Timer().schedule(myTimerTask, 1000)
    }


    @SuppressLint("SetTextI18n")
    private fun refreshUi() {
        runOnUiThread {
            mBinding.apply {
                tvLevel.text = "等级：$level"
                tvAttack.text = "攻击力：$attack"
                tvExp.text = "当前经验值：$expCurrent，还差${expTotal - expCurrent}经验升级"
                tvEnemyHp.text = "敌人血量：$enemyHpCurrent"
                tvSpeed.text = "当前攻击速度：1次/${speed}ms"
                tvAttackRadio.text = "练功加成：${attackGetRadio * 100}%"
                tvExpRadio.text = "经验加成：${expGetRadio * 100}%"
                tvSpeedRadio.text = "攻速加成：${speedGetRadio * 100}%"
                btnUpAttack.text = "提升攻击加成，消耗${attackGetRadio}点等级"
                btnUpExp.text = "提升经验加成，消耗${expGetRadio}点等级"
                btnUpSpeed.text = "提升攻速加成，消耗${speedGetRadio}点等级"
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initEvent() {
        mBinding.apply {
            btnAttack.setOnClickListener {
                attack()
                addLog("主动攻击了一次，造成${attack}点伤害")
            }
            btnStudy.setOnClickListener {
                attack += attackGetRadio
                addLog("练功一次，增加了${attackGetRadio}点伤害")
                refreshUi()
            }
            btnUpAttack.setOnClickListener {
                if (level > attackGetRadio) {
                    level -= attackGetRadio
                    attackGetRadio++
                    refreshUi()
                } else {
                    Toast.makeText(this@MainActivity, "升级失败", Toast.LENGTH_SHORT).show()
                }
            }
            btnUpExp.setOnClickListener {
                if (level > expGetRadio) {
                    level -= expGetRadio
                    expGetRadio++
                    refreshUi()
                } else {
                    Toast.makeText(this@MainActivity, "升级失败", Toast.LENGTH_SHORT).show()
                }
            }
            btnUpSpeed.setOnClickListener {
                if (level > speedGetRadio) {
                    level -= speedGetRadio
                    speedGetRadio++
                    speed = 2000L / speedGetRadio
                    if (speed < 5L) {
                        speed = 5
                    }
                    refreshUi()
                } else {
                    Toast.makeText(this@MainActivity, "升级失败", Toast.LENGTH_SHORT).show()
                }
            }
            canvas.setOnClickListener { btnAttack.callOnClick() }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addLog(log: String) {
        runOnUiThread {
            if (logs.size > 100) {
                logs.clear()
            }
            logs.add(0, log)
            adapter.notifyDataSetChanged()
        }
    }

    private fun attack() {
        runOnUiThread {
            val imageView = ImageView(this)
            imageView.setImageResource(R.drawable.ic_baseline_brightness_2_24)
            val width = mBinding.player.layoutParams.width
            val height = mBinding.player.layoutParams.height

            imageView.layoutParams = RelativeLayout.LayoutParams(width, height)
            imageView.setPadding(0, 40, 0, 0)
            mBinding.canvas.addView(imageView)
            val x = mBinding.npc.x - mBinding.player.x - mBinding.npc.layoutParams.width / 2

            val animator = ObjectAnimator.ofFloat(imageView, "translationX", width.toFloat(), x)
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                @SuppressLint("SetTextI18n")
                override fun onAnimationEnd(animation: Animator?) {
                    mBinding.canvas.removeView(imageView)
                    val textView = TextView(this@MainActivity)
                    textView.text = "-$attack"
                    textView.x = mBinding.npc.x + 40
                    textView.y = mBinding.npc.y
                    textView.setTextColor(Color.RED)
                    textView.layoutParams = RelativeLayout.LayoutParams(width, height)
                    val anim = ObjectAnimator.ofFloat(textView, "translationY", mBinding.npc.y, -30f)
                    anim.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            mBinding.canvas.removeView(textView)
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                    })
                    mBinding.canvas.addView(textView)
                    anim.duration = 2000
                    anim.start()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

            })
            animator.duration = 2000
            animator.start()

        }
        enemyHpCurrent -= attack
        if (enemyHpCurrent <= 0) {
            //击杀
            expCurrent += enemyHpTotal * expGetRadio
            addLog("成功击杀, 获得${enemyHpTotal * expGetRadio}点经验")
            val anim = ObjectAnimator.ofFloat(mBinding.npc, "rotation", 0f, 360f)
            anim.duration = 1000
            anim.start()
            if (expCurrent >= expTotal) {
                //升级
                expCurrent -= expTotal
                upLevel()
            }
            enemyHpCurrent = enemyHpTotal
        }
        refreshUi()
        //test
        //123
    }

    private fun upLevel() {
        addLog("等级提升")
        level++
        enemyHpTotal = level * enemyHpRatio
        expTotal = level * expRatio
    }


    override fun onDestroy() {
        mmkv.putLong("level", level)
        mmkv.putLong("exp", expCurrent)
        exitTime = Date().time
        mmkv.putLong("exitTime", exitTime)
        mmkv.putLong("attack", attack)
        mmkv.putLong("speed", speed)
        mmkv.putLong("attackGetRadio", attackGetRadio)
        mmkv.putLong("expGetRadio", expGetRadio)
        mmkv.putLong("speedGetRadio", speedGetRadio)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    inner class MyTimerTask : TimerTask() {
        @SuppressLint("NotifyDataSetChanged")
        override fun run() {
            while (true) {
                runOnUiThread {
                    attack()
                    addLog("自动攻击了一次，造成${attack}点伤害")
                }
                Thread.sleep(speed)
            }
        }
    }
}