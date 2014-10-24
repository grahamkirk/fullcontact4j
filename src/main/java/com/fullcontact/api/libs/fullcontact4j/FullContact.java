package com.fullcontact.api.libs.fullcontact4j;


import com.fullcontact.api.libs.fullcontact4j.config.FCConstants;
import com.fullcontact.api.libs.fullcontact4j.enums.RateLimiterPolicy;
import com.fullcontact.api.libs.fullcontact4j.request.*;
import com.fullcontact.api.libs.fullcontact4j.response.FCResponse;
import com.squareup.okhttp.OkHttpClient;
import retrofit.client.Client;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FullContact {
    protected static Level logLevel = Level.OFF;

    /**
     * This interface holds references to all the objects used by a FullContact client to communicate with the api,
     * convert responses, etc.
     */
    protected FullContactHttpInterface httpInterface;

    protected FullContact(Client httpClient, RateLimiterPolicy policy, String baseUrl,
                        Integer threadPoolCount) {
        httpInterface = new FullContactHttpInterface(httpClient, policy, baseUrl, threadPoolCount);
        Utils.info("Created new FullContact client.");
    }

    /**
     * Set what level to log at (default OFF). Level.INFO will post important logs (client creations, errors).
     * Level.FINE will log FullContact4j's workflow.
     * @param log
     */
    public static void setLogLevel(Level log) {
        logLevel = log;
    }

    /**
     * Initializes a new FullContact Client Builder with an api key.
     * This is the most common way to create a client.
     * @param apiKey an api key associated with an account.
     * @return a new FullContact client builder
     */
    public static Builder withApiKey(String apiKey) {
        return new Builder(apiKey);
    }


    //api solution - create new client extends retrofit.Client
    // public void execute(Request) {
    // handleHeaders();
    // coreClient.execute();
    // }
    //TODO update /developer/docs/libraries/
    /////API Methods//////

    /**
     * Creates a new generic request builder, where parameters and endpoint are all customizable.
     */
    public GenericRequest.Builder buildGenericRequest() {
        return new GenericRequest.Builder();
    }

    /**
     * Creates a new Person search.
     */
    public PersonRequest.Builder buildPersonRequest() { return new PersonRequest.Builder(); }

    /**
     * Upload a new card to be processed by Card Reader.
     * @param front a ByteArrayOutputStream representing the picture of the front of the card
     */
    public UploadCardRequest.Builder buildUploadCardRequest(InputStream front) { return new UploadCardRequest.Builder().cardFront(front); }

    /**
     * View a single card.
     * @param id the card's ID
     */
    public CardReaderViewRequest.Builder buildCardReaderViewRequest(String id) { return new CardReaderViewRequest.Builder().cardId(id); }

    /**
     * View a history of your Card Reader requests with this api key, beginning with the earliest.
     */
    public CardReaderViewAllRequest.Builder buildCardReaderViewAllRequest() { return new CardReaderViewAllRequest.Builder(); }

    /**
     * Create a new disposable email check
     * @return a disposable email request builder with a pre-configured email to check
     */
    public DisposableEmailRequest.Builder buildDisposableEmailRequest(String email) { return new DisposableEmailRequest.Builder().email(email); }

    /**
     * Normalize a given name
     * @return a name normalization request builder
     */
    public NameNormalizationRequest.Builder buildNameNormalizationRequest(String name) { return new NameNormalizationRequest.Builder().query(name); }

    /**
     * Determine, given two names, which was a family name and which was a first name.
     * @return a parse request builder, pre-configured with the name
     */
    public NameParseRequest.Builder buildNameParseRequest(String name) { return new NameParseRequest.Builder().name(name); }

    /**
     * Deduces someone's real name from a username or email.
     */
    public NameDeduceRequest.Builder buildNameDeduceRequest() { return new NameDeduceRequest.Builder(); }

    /**
     * Use a variety of algorithms to determine the similarity of two names.
     * @return a name similarity request builder, pre-configured with the two names
     */
    public NameSimilarityRequest.Builder buildNameSimilarityRequest(String name1, String name2) { return new NameSimilarityRequest.Builder().name1(name1).name2(name2); }

    public NameStatsRequest.Builder buildNameStatsRequest() { return new NameStatsRequest.Builder(); }

    /**
     * Normalize a location from a given string
     * @return a location normalization request builder, pre-configured with the location
     */
    public LocationNormalizationRequest.Builder buildLocationNormalizationRequest(String place) { return new LocationNormalizationRequest.Builder().place(place); }

    /**
     * Enrich (gather more data about) a given location from a string.
     * @return a location enrichment request builder, pre-configured with the location
     */
    public LocationEnrichmentRequest.Builder buildLocationEnrichmentRequest(String place) { return new LocationEnrichmentRequest.Builder().place(place); }

    /**
     * Request stats about the api key in use.
     */
    public AccountStatsRequest.Builder buildAccountStatsRequest() { return new AccountStatsRequest.Builder(); }

    /**
     * Makes a synchronous request to the FullContact APIs.
     * @throws FullContactException if the request fails, this method will throw a FullContactException with a reason.
     * @param req the request, generated with a call to build____Request().
     * @param <T> the Response type
     * @return if the request is successful, this method returns the corresponding {@link com.fullcontact.api.libs.fullcontact4j.response.FCResponse}.
     */
    public <T extends FCResponse> T sendRequest(FCRequest<T> req) throws FullContactException {
        return httpInterface.sendRequest(req);
    }

    /**
     * Makes an asynchronous request to the FullContact APIs.
     * Exceptions will call {@link com.fullcontact.api.libs.fullcontact4j.request.FCCallback#failure(FullContactException)}.
     * Successful responses will call {@link com.fullcontact.api.libs.fullcontact4j.request.FCCallback#success(com.fullcontact.api.libs.fullcontact4j.response.FCResponse)}
     * @param req the request, generated with a call to build____Request().
     * @param callback your callback
     * @param <T> the Response type
     */
    public <T extends FCResponse> void sendRequest(FCRequest<T> req, FCCallback<T> callback) {
        httpInterface.sendRequest(req, callback);
    }


    /////////////////////


    public static class Builder {

        private String authKey;
        private OkHttpClient httpClient = new OkHttpClient();
        private Integer threadPoolCount = 1;
        private String baseUrl = FCConstants.API_BASE_DEFAULT;
        private RateLimiterPolicy ratePolicy = RateLimiterPolicy.SMOOTH;

        public Builder(String apiKey) {
            //default client is OkHttpClient
            this.authKey = apiKey;
        }

        /**
         * What HTTP client FullContact should use; useful to configure read timeouts, etc.
         * By default, FullContact uses a default {@link retrofit.client.OkClient}.
         * @param client the configured http client
         * @return
         */
        public Builder httpClient(OkHttpClient client) {
            httpClient = client;
            return this;
        }

        /**
         * How many threads to use to execute API queries (default 1).
         * @param threads
         * @return
         */
        public Builder threadCount(Integer threads) {
            threadPoolCount = threads;
            return this;
        }

        /**
         * Sets the read timeout.
         */
        public Builder setReadTimeout(Integer timeoutMs) {
            httpClient.setReadTimeout(timeoutMs, TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * Sets the connect timeout.
         * @param timeoutMs
         * @return
         */
        public Builder setConnectTimeout(Integer timeoutMs) {
            httpClient.setConnectTimeout(timeoutMs, TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * Sets the base URL that the client accesses FullContact's endpoints from.
         * This defaults to the standard api.fullcontact.com/v2/.
         * @param url
         * @return
         */
        public Builder baseUrl(String url) {
            baseUrl = url;
            return this;
        }

        /**
         * @see com.fullcontact.api.libs.fullcontact4j.enums.RateLimiterPolicy
         */
        public Builder rateLimiterPolicy(RateLimiterPolicy policy) {
            this.ratePolicy = policy;
            return this;
        }

        /**
         * Builds a new FullContact client.
         * @return a new, fully configured, FullContact client.
         */
        public FullContact build() {
            if(authKey == null || authKey.isEmpty()) {
                throw new IllegalArgumentException("Authentication key cannot be null");
            }

            return new FullContact(new FullContactHttpInterface.DynamicHeaderOkClient(httpClient, authKey), ratePolicy, baseUrl, threadPoolCount);
        }
    }


}