package com.pukkol.app_settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {
    private var mRemovedApps = ArrayList<String>()
    var mSettingsFragment : SettingsFragment? = null

    companion object {
        fun dataRemoveKey() : String {
            return "RemovedPackages"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // get intents
        try {
            mRemovedApps = this.intent.getStringArrayListExtra(dataRemoveKey()) as ArrayList<String>
        } catch (exception: TypeCastException) {
            Log.d("RemoveAppsActivity", "Intent data is empty please add a Intent with the key: `${dataRemoveKey()}` of `ArrayList<String>` with Removed app package names")
            exception.printStackTrace()
        }

        mSettingsFragment = SettingsFragment(mRemovedApps)
        this.supportFragmentManager.beginTransaction()
                .replace(settings_frameLayout.id, mSettingsFragment!!)
                .addToBackStack(null)
                .commit()
    }

    override fun onStop() {
        setIntentResults()
        super.onStop()
    }

    override fun onBackPressed() {
        setIntentResults()
        super.onBackPressed()
    }

    private fun setIntentResults() {
        for (element in mSettingsFragment!!.getData()) {
            intent.putExtra(element.key, element.value)
        }

        setResult(Activity.RESULT_OK, intent)
    }

    class SettingsFragment(removeApps: ArrayList<String>) : PreferenceFragmentCompat() {
        private val mRemovedApps : ArrayList<String> = removeApps
        private var mData = HashMap<String, ArrayList<String>>()

        // Simple StartActivityForResult
        private val simpleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.getStringArrayListExtra(dataRemoveKey())
                data?.let { mData.put(dataRemoveKey(), it) }
            }
        }

        fun getData() : HashMap<String, ArrayList<String>> {
            return mData
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)

            // remove applications
            findPreference<Preference>("remove_apps")?.setOnPreferenceClickListener {
                val removeAppsIntent = Intent(activity, RemoveAppsActivity::class.java)
                        .putExtra(dataRemoveKey(), mRemovedApps)


                simpleLauncher.launch(removeAppsIntent)
                true
            }

        }

    }


}

