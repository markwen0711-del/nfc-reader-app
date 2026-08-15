package com.example.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var tvCardType: TextView
    private lateinit var tvHex: TextView
    private lateinit var tvDecBig: TextView
    private lateinit var tvDecLittle: TextView
    private lateinit var tvExtra: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCardType = findViewById(R.id.tvCardType)
        tvHex = findViewById(R.id.tvHex)
        tvDecBig = findViewById(R.id.tvDecBig)
        tvDecLittle = findViewById(R.id.tvDecLittle)
        tvExtra = findViewById(R.id.tvExtra)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            tvCardType.text = "此裝置不支援 NFC"
        } else if (!nfcAdapter!!.isEnabled) {
            tvCardType.text = "請先到系統設定開啟 NFC"
        }

        // 如果是被NFC Intent啟動的(App冷啟動時掃到卡)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // 前景派發模式: 讓App在開啟狀態下優先攔截所有掃到的卡片
    private fun enableForegroundDispatch() {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, pendingIntentFlags
        )

        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )

        val techLists = arrayOf(
            arrayOf(NfcA::class.java.name),
            arrayOf(NfcB::class.java.name),
            arrayOf(NfcF::class.java.name),
            arrayOf(NfcV::class.java.name),
            arrayOf(MifareClassic::class.java.name)
        )

        adapter.enableForegroundDispatch(this, pendingIntent, filters, techLists)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        if (action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.let { processTag(it) }
        }
    }

    private fun processTag(tag: Tag) {
        val uidBytes = tag.id // byte array, 通常是 4, 7 或 10 bytes

        // 1. 十六進位字串 (大寫, byte順序 = 讀取順序 / Big-Endian顯示)
        val hexString = uidBytes.joinToString(separator = " ") {
            String.format("%02X", it)
        }

        // 2. 十進位 - 依卡片原始bytes順序當作Big-Endian大數
        val decBig = BigInteger(1, uidBytes)

        // 3. 十進位 - 反轉byte順序當作Little-Endian(很多門禁讀卡機/考勤機顯示的10進位卡號用這種)
        val reversedBytes = uidBytes.reversedArray()
        val decLittle = BigInteger(1, reversedBytes)

        // 4. 卡片技術類型
        val techList = tag.techList.joinToString(", ") { it.substringAfterLast('.') }

        // 5. 額外資訊: ATQA / SAK (若是 MifareClassic / NfcA)
        val extraInfo = StringBuilder()
        val nfcA = NfcA.get(tag)
        if (nfcA != null) {
            val atqa = nfcA.atqa.joinToString(" ") { String.format("%02X", it) }
            val sak = String.format("%02X", nfcA.sak)
            extraInfo.append("ATQA: $atqa, SAK: $sak\n")
        }
        extraInfo.append("UID長度: ${uidBytes.size} bytes")

        runOnUiThread {
            tvCardType.text = "卡片類型: $techList"
            tvHex.text = hexString
            tvDecBig.text = decBig.toString()
            tvDecLittle.text = decLittle.toString()
            tvExtra.text = extraInfo.toString()
        }
    }
}
