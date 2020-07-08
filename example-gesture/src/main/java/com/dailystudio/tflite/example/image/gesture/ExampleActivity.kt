package com.dailystudio.tflite.example.image.gesture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.fragment.AbsRecyclerViewFragment
import com.dailystudio.devbricksx.utils.StringUtils
import com.dailystudio.tflite.example.common.AbsExampleActivity
import com.dailystudio.tflite.example.common.InferenceInfo
import com.dailystudio.tflite.example.image.gesture.fragment.GestureCameraFragment
import com.dailystudio.tflite.example.image.gesture.fragment.GestureLabel
import com.dailystudio.tflite.example.image.gesture.fragment.fragment.GestureLabelsListFragment
import com.dailystudio.tflite.example.image.gesture.fragment.model.GestureLabelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.examples.gesture.Classifier

class ExampleActivity : AbsExampleActivity<InferenceInfo, List<Classifier.Recognition>>() {

    companion object {
        const val FRAGMENT_TAG_RESULTS = "results-fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            initResults()
        }
    }

    override fun onResume() {
        super.onResume()

        disableClicksOnResultsFragment()
    }

    override fun createBaseFragment(): Fragment {
        return GestureCameraFragment()
    }

    override fun createResultsView(): View? {
        val stubView = LayoutInflater.from(this).inflate(
            R.layout.layout_results_view, null)

        supportFragmentManager.beginTransaction().also {
            val resultsFragment = GestureLabelsListFragment()

            it.add(R.id.results_fragment_stub, resultsFragment, FRAGMENT_TAG_RESULTS)
            it.show(resultsFragment)
            it.commitAllowingStateLoss()
       }

        return stubView
    }

    override fun createSettingsView(): View? {
        return null
    }

    override fun onResultsUpdated(results: List<Classifier.Recognition>) {
        val selectedGesture = if (results.isNotEmpty()) {
            results[0]
        } else {
            null
        }

        Logger.debug("selectedGesture = ${selectedGesture.toString().replace("%", "%%")}")

        val viewModel = ViewModelProvider(this).get(GestureLabelViewModel::class.java)

        val gestureLabels = viewModel.getGestureLabels()
        for (gl in gestureLabels) {
            gl.selected = selectedGesture?.let {
                it.title == gl.label
            } ?: false

            Logger.debug("updated-gl = $gl")

            viewModel.updateGestureLabel(gl)
        }
    }

    private fun initResults() {
        val viewModel = ViewModelProvider(this).get(GestureLabelViewModel::class.java)

        val labels = StringUtils.linesFromAsset(this,"labels.txt")
        for ((i, label) in labels.withIndex()) {
            viewModel.insertGestureLabel(GestureLabel(i, label))
        }
    }

    private fun disableClicksOnResultsFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_RESULTS)
        if (fragment is AbsRecyclerViewFragment<*, *, *>) {
            fragment.setRecyclerViewTouchEnabled(false)
        }
    }

}