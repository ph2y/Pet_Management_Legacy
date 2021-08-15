package com.sju18001.petmanagement.ui.community.followerFollowing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.ActivitySearchBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.FetchAccountReqDto
import com.sju18001.petmanagement.restapi.dto.FetchAccountResDto
import com.sju18001.petmanagement.restapi.dto.LoginResDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class SearchActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivitySearchBinding

    // session manager for user token
    private lateinit var sessionManager: SessionManager

    // pattern regex for EditText
    private val patternUsername: Pattern = Pattern.compile("^[a-zA-Z0-9가-힣_]{2,20}$")

    // variable for ViewModel
    private val searchViewModel: SearchViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(SearchViewModel::class.java)
    }

    // variable for storing API call(for cancel)
    private var fetchAccountApiCall: Call<FetchAccountResDto>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get session manager
        sessionManager = SessionManager(this)

        // no title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        // view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // for search EditText listener
        binding.searchEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchViewModel.searchEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for search button
        binding.searchButton.setOnClickListener {
            // verify regex + show message if invalid
            if(!patternUsername.matcher(searchViewModel.searchEditText).matches()) {
                Toast.makeText(this, getString(R.string.nickname_regex_exception_message), Toast.LENGTH_LONG).show()
            }
            else {
                // set api state/button to loading
                searchViewModel.apiIsLoading = true
                setSearchButtonToLoading()

                searchAccount(searchViewModel.searchEditText)
            }
        }

        // for close button
        binding.closeButton.setOnClickListener { finish() }

        // for hiding keyboard
        Util.setupViewsForHideKeyboard(this, binding.activitySearchParentLayout)
    }

    override fun onStart() {
        super.onStart()

        // restore views
        // TODO
    }

    private fun setSearchButtonToLoading() {
        binding.searchButton.isEnabled = false
    }

    private fun setSearchButtonToNormal() {
        binding.searchButton.isEnabled = true
    }

    private fun searchAccount(nickname: String) {
        // create DTO
        val fetchAccountReqDto = FetchAccountReqDto(null, null, nickname)

        // API call
        fetchAccountApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchAccountNicknameReq(fetchAccountReqDto)
        fetchAccountApiCall!!.enqueue(object: Callback<FetchAccountResDto> {
            override fun onResponse(
                call: Call<FetchAccountResDto>,
                response: Response<FetchAccountResDto>
            ) {
                if(response.isSuccessful) {
                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                    setSearchButtonToNormal()

                    Log.d("test", response.body().toString())
                }
                else {
                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                    setSearchButtonToNormal()

                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchAccountResDto>, t: Throwable) {
                // set api state/button to normal
                searchViewModel.apiIsLoading = false
                setSearchButtonToNormal()

                // if the view was destroyed(API call canceled) -> return
                // TODO
                if(binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(this@SearchActivity, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        // stop api call when fragment is destroyed
        fetchAccountApiCall?.cancel()
    }
}