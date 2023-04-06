package com.ess.manager_ui_native

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels

private const val FEEDBACK_TEXT = "feedbackText"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedbackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedbackFragment : Fragment() {
    private val redeemViewModel: RedeemViewModel by activityViewModels()
    private lateinit var feedbackTextView: TextView
    private lateinit var actionButton: Button
    private var feedbackText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            feedbackText = it.getString(FEEDBACK_TEXT, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        feedbackTextView = requireView().findViewById(R.id.feedback_tv)
        actionButton = requireView().findViewById(R.id.action_button)
        feedbackTextView.text = feedbackText

        if (feedbackText.contains("success")) {
            actionButton.text = getString(R.string.done)

        } else {
            actionButton.text = getString(R.string.cancel)
        }
        actionButton.setOnClickListener {
            redeemViewModel.setScreen(RedeemScreen.Validate)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(feedbackText: String) =
            FeedbackFragment().apply {
                arguments = Bundle().apply {
                    putString(FEEDBACK_TEXT, feedbackText)
                }
            }
    }
}