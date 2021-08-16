package com.sju18001.petmanagement.ui.community.followerFollowing

import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.ActivitySearchBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dto.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
    private var fetchAccountPhotoApiCall: Call<ResponseBody>? = null
    private var fetchFollowerApiCall: Call<FetchFollowerResDto>? = null
    private var createFollowApiCall: Call<CreateFollowResDto>? = null
    private var deleteFollowApiCall: Call<DeleteFollowResDto>? = null


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

        // for follow unfollow button
        binding.followUnfollowButton.setOnClickListener {
            // set API/button state to loading
            searchViewModel.apiIsLoading = true
            setButtonState()

            // API call
            if(searchViewModel.accountId !in searchViewModel.followerIdList!!) {
                createFollow()
            }
            else {
                deleteFollow()
            }
        }

        // for close button
        binding.closeButton.setOnClickListener { finish() }

        // for hiding keyboard
        Util.setupViewsForHideKeyboard(this, binding.activitySearchParentLayout)
    }

    override fun onStart() {
        super.onStart()

        // initialize follower id list
        if(searchViewModel.followerIdList == null) {
            updateFollowerIdList()
        }

        // restore views
        restoreState()
    }

    private fun setSearchButtonToLoading() {
        binding.searchButton.isEnabled = false
    }

    private fun setSearchButtonToNormal() {
        binding.searchButton.isEnabled = true
    }

    private fun setAccountInfoViews(fetchAccountResDto: FetchAccountResDto) {
        // set layout's visibility to visible(if not already done)
        if(binding.accountInfoCardView.visibility != View.VISIBLE) {
            binding.accountInfoCardView.visibility = View.VISIBLE
        }
        
        // if url is not null -> fetch photo and set it
        if(fetchAccountResDto.photoUrl != null) {
            searchViewModel.accountPhotoUrl = fetchAccountResDto.photoUrl
            fetchAccountPhoto(fetchAccountResDto.id)
        }
        // else -> reset photo related values
        else {
            searchViewModel.accountPhotoUrl = null
            searchViewModel.accountPhotoByteArray = null
            setAccountPhoto()
        }

        // save id value
        searchViewModel.accountId = fetchAccountResDto.id

        // save and set nickname value
        searchViewModel.accountNickname = fetchAccountResDto.nickname
        val nicknameText = searchViewModel.accountNickname + '님'
        binding.accountNickname.text = nicknameText

        // set button status
        setButtonState()
    }

    private fun setAccountPhoto() {
        if(searchViewModel.accountPhotoUrl != null) {
            binding.accountPhoto.setImageBitmap(BitmapFactory.decodeByteArray(searchViewModel.accountPhotoByteArray,
                0, searchViewModel.accountPhotoByteArray!!.size))
        }
        else {
            binding.accountPhoto.setImageDrawable(getDrawable(R.drawable.ic_baseline_account_circle_24))
        }
    }

    private fun setButtonState() {
        // exception
        if(searchViewModel.accountId == null) { return }

        // if id not in follower id list -> set button to follow
        if(searchViewModel.accountId !in searchViewModel.followerIdList!!) {
            binding.followUnfollowButton.background
                .setColorFilter(ContextCompat.getColor(this, R.color.carrot), PorterDuff.Mode.MULTIPLY)
            binding.followUnfollowButton.setTextColor(resources.getColor(R.color.white))
            binding.followUnfollowButton.text = getText(R.string.follow_button)
        }

        // if id in follower id list -> set button to unfollow
        else {
            binding.followUnfollowButton.background
                .setColorFilter(ContextCompat.getColor(this, R.color.border_line), PorterDuff.Mode.MULTIPLY)
            binding.followUnfollowButton.setTextColor(resources.getColor(R.color.black))
            binding.followUnfollowButton.text = getText(R.string.unfollow_button)
        }

        // if API is loading -> set button to loading, else -> set button to normal
        binding.followUnfollowButton.isEnabled = !searchViewModel.apiIsLoading
    }

    private fun updateFollowerIdList() {
        // reset list
        searchViewModel.followerIdList = mutableListOf()

        // create empty body
        val emptyBody = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        // API call
        fetchFollowerApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchFollowerReq(emptyBody)
        fetchFollowerApiCall!!.enqueue(object: Callback<FetchFollowerResDto> {
            override fun onResponse(
                call: Call<FetchFollowerResDto>,
                response: Response<FetchFollowerResDto>
            ) {
                if(response.isSuccessful) {
                    response.body()!!.followerList.map {
                        searchViewModel.followerIdList!!.add(it.id)
                    }

                    // update button state
                    setButtonState()
                }
                else {
                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<FetchFollowerResDto>, t: Throwable) {
                // if API call was canceled -> return
                if(searchViewModel.apiIsCanceled) {
                    searchViewModel.apiIsCanceled = false
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(this@SearchActivity, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun searchAccount(nickname: String) {
        // create DTO
        val fetchAccountReqDto = FetchAccountReqDto(null, null, nickname)

        // API call
        fetchAccountApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchAccountByNicknameReq(fetchAccountReqDto)
        fetchAccountApiCall!!.enqueue(object: Callback<FetchAccountResDto> {
            override fun onResponse(
                call: Call<FetchAccountResDto>,
                response: Response<FetchAccountResDto>
            ) {
                if(response.isSuccessful) {
                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                    setSearchButtonToNormal()

                    // set account info views
                    setAccountInfoViews(response.body()!!)
                }
                else {
                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                    setSearchButtonToNormal()

                    // get error message + handle exceptions
                    when(val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)) {
                        // if no such account exists -> show Toast message
                        "Account not exists" -> {
                            Toast.makeText(this@SearchActivity,
                                getText(R.string.account_does_not_exist_exception_message), Toast.LENGTH_LONG).show()
                        }
                        // if fetched self -> show Toast message
                        "Fetched self" -> {
                            Toast.makeText(this@SearchActivity,
                                getText(R.string.fetched_self_exception_message), Toast.LENGTH_LONG).show()
                        }
                        // other exceptions -> show Toast message + log
                        else -> {
                            Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_LONG).show()
                            Log.d("error", errorMessage)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<FetchAccountResDto>, t: Throwable) {
                // set api state/button to normal
                searchViewModel.apiIsLoading = false
                setSearchButtonToNormal()

                // if API call was canceled -> return
                if(searchViewModel.apiIsCanceled) {
                    searchViewModel.apiIsCanceled = false
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(this@SearchActivity, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun fetchAccountPhoto(id: Long) {
        // API call
        fetchAccountPhotoApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .fetchAccountPhotoByIdReq(id)
        fetchAccountPhotoApiCall!!.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful) {
                    // save photo as byte array
                    searchViewModel.accountPhotoByteArray = response.body()!!.byteStream().readBytes()

                    // set account photo
                    setAccountPhoto()
                }
                else {
                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // if API call was canceled -> return
                if(searchViewModel.apiIsCanceled) {
                    searchViewModel.apiIsCanceled = false
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(this@SearchActivity, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun createFollow() {
        // create DTO
        val createFollowReqDto = CreateFollowReqDto(searchViewModel.accountId!!)

        // API call
        createFollowApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .createFollowReq(createFollowReqDto)
        createFollowApiCall!!.enqueue(object: Callback<CreateFollowResDto> {
            override fun onResponse(
                call: Call<CreateFollowResDto>,
                response: Response<CreateFollowResDto>
            ) {
                if(response.isSuccessful) {
                    // update follower id list(update button state)
                    updateFollowerIdList()

                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                }
                else {
                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                    setButtonState()

                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<CreateFollowResDto>, t: Throwable) {
                // if API call was canceled -> return
                if(searchViewModel.apiIsCanceled) {
                    searchViewModel.apiIsCanceled = false
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(this@SearchActivity, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun deleteFollow() {
        // create DTO
        val deleteFollowReqDto = DeleteFollowReqDto(searchViewModel.accountId!!)

        // API call
        deleteFollowApiCall = RetrofitBuilder.getServerApiWithToken(sessionManager.fetchUserToken()!!)
            .deleteFollowReq(deleteFollowReqDto)
        deleteFollowApiCall!!.enqueue(object: Callback<DeleteFollowResDto> {
            override fun onResponse(
                call: Call<DeleteFollowResDto>,
                response: Response<DeleteFollowResDto>
            ) {
                if(response.isSuccessful) {
                    // update follower id list(update button state)
                    updateFollowerIdList()

                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                }
                else {
                    // set api state/button to normal
                    searchViewModel.apiIsLoading = false
                    setButtonState()

                    // get error message
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                    // Toast + Log
                    Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<DeleteFollowResDto>, t: Throwable) {
                // if API call was canceled -> return
                if(searchViewModel.apiIsCanceled) {
                    searchViewModel.apiIsCanceled = false
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(this@SearchActivity, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun restoreState() {
        // restore EditText
        binding.searchEditText.setText(searchViewModel.searchEditText)

        // restore account info layout
        if(searchViewModel.accountId != null) {
            binding.accountInfoCardView.visibility = View.VISIBLE

            // account photo
            if(searchViewModel.accountPhotoUrl != null) {
                binding.accountPhoto.setImageBitmap(BitmapFactory.decodeByteArray(searchViewModel.accountPhotoByteArray,
                    0, searchViewModel.accountPhotoByteArray!!.size))
            }

            // account nickname
            val nicknameText = searchViewModel.accountNickname + '님'
            binding.accountNickname.text = nicknameText

            // follow unfollow button
            setButtonState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // stop api call when fragment is destroyed
        searchViewModel.apiIsCanceled = true
        fetchAccountApiCall?.cancel()
        fetchAccountPhotoApiCall?.cancel()
        fetchFollowerApiCall?.cancel()
        createFollowApiCall?.cancel()
        deleteFollowApiCall?.cancel()
    }
}