package com.fullcontact.api.libs.fullcontact4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullcontact.api.libs.fullcontact4j.enums.CardReaderQuality;
import com.fullcontact.api.libs.fullcontact4j.http.WebhookResponse;
import com.fullcontact.api.libs.fullcontact4j.http.cardreader.CardReaderFullResponse;
import com.fullcontact.api.libs.fullcontact4j.http.cardreader.CardReaderUploadConfirmResponse;
import com.fullcontact.api.libs.fullcontact4j.http.cardreader.CardReaderViewAllResponse;
import com.fullcontact.api.libs.fullcontact4j.http.company.CompanyResponse;
import com.fullcontact.api.libs.fullcontact4j.http.company.model.KeyPerson;
import com.fullcontact.api.libs.fullcontact4j.http.location.LocationEnrichmentResponse;
import com.fullcontact.api.libs.fullcontact4j.http.location.LocationNormalizationResponse;
import com.fullcontact.api.libs.fullcontact4j.http.misc.AccountStatsResponse;
import com.fullcontact.api.libs.fullcontact4j.http.misc.DisposableEmailResponse;
import com.fullcontact.api.libs.fullcontact4j.http.name.NameParseResponse;
import com.fullcontact.api.libs.fullcontact4j.http.name.NameResponse;
import com.fullcontact.api.libs.fullcontact4j.http.name.NameSimilarityResponse;
import com.fullcontact.api.libs.fullcontact4j.http.name.NameStatsResponse;
import com.fullcontact.api.libs.fullcontact4j.http.person.PersonResponse;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResponseModelTest {
    public static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void personDeserializationTest() throws IOException {
        PersonResponse r = mapper.readValue(Utils.loadFile("example-person-response.json"), PersonResponse.class);
        assertTrue(r.getDemographics().getGender().equals("Male"));
    }

    @Test
    public void personQueue202Test() throws IOException {
        PersonResponse r = mapper.readValue(Utils.loadFile("example-person-queue-response.json"), PersonResponse.class);
        assertEquals(202, r.getStatus());
        assertTrue(r.getMessage().contains("Queued for search"));
    }

    private static KeyPerson travisTodd = new KeyPerson("Travis Todd", "Co-Founder",
            "https://api.fullcontact.com/v2/person.json?lookup=pDF50R_M0Gw8OfXk4zQXZBhkY_WkYdgF7SulvVo4uE0=");
    private static KeyPerson edenElder = new KeyPerson("Eden Elder, Ph.D.",
            "Chief People Officer", null);

    @Test
    public void companyDeserializationTest() throws IOException {
        CompanyResponse r = mapper.readValue(Utils.loadFile("example-company-response.json"), CompanyResponse.class);

        assertTrue(r.getLogo().contains("cloudfront"));
        assertEquals("en", r.getLanguageLocale());
        assertEquals("2010", r.getOrganization().getFounded());
        assertEquals("FullContact Inc.", r.getOrganization().getName());
        assertEquals("Colorado", r.getOrganization().getContactInfo().getAddresses().get(0).getRegion().getName());
        assertEquals("+1 (888) 330-6943", r.getOrganization().getContactInfo().getPhoneNumbers().get(0).getNumber());
        assertTrue(r.getOrganization().getLinks().get(0).getUrl().contains("fullcontact"));
        assertTrue(r.getOrganization().getImages().get(4).getUrl().contains("cloudfront"));
        assertTrue(r.getOrganization().getKeywords().contains("Software"));
        assertEquals(11, r.getSocialProfiles().size());
        assertEquals("107620035082673219790", r.getSocialProfiles().get(5).getId());
        assertEquals("us", r.getTraffic().getTopCountryRanking().get(0).getLocale());
        assertEquals(8, r.getOrganization().getKeyPeople().size());
        assertTrue(r.getOrganization().getKeyPeople().contains(travisTodd));
        assertTrue(r.getOrganization().getKeyPeople().contains(edenElder));
    }

    @Test
    public void canDeserializePersonWebhookJSON() throws Exception {
        WebhookResponse<PersonResponse> wr = WebhookResponse.fromJson(Utils.loadFileAsString("person-webhook-json.json"), PersonResponse.class);
        assertEquals("ericperson", wr.getWebhookId());

        PersonResponse r = wr.getResult();
        assertEquals(200, r.getStatus());
        assertTrue(r.getSocialProfiles().size() > 0);
    }

    @Test
    public void canDeserializeCompanyWebhookJSON() throws Exception {
        WebhookResponse<CompanyResponse> wr = WebhookResponse.fromJson(Utils.loadFileAsString("company-webhook-json.json"), CompanyResponse.class);
        assertEquals("ericcompany", wr.getWebhookId());

        CompanyResponse r = wr.getResult();
        assertEquals(200, r.getStatus());
        assertTrue(r.getSocialProfiles().size() > 0);
    }

    @Test
    public void keyPersonToRequestTest() {
        assertEquals("pDF50R_M0Gw8OfXk4zQXZBhkY_WkYdgF7SulvVo4uE0=",
                travisTodd.toPersonRequestOrNull().build().getParam("lookup"));
        assertEquals(null, edenElder.toPersonRequestOrNull());
    }

    @Test
    public void cardReaderAcceptDeserializationTest() throws Exception {
        CardReaderUploadConfirmResponse conf = mapper.readValue(Utils.loadFile("example-upload-confirm-response.json"), CardReaderUploadConfirmResponse.class);
        assertEquals(conf.getEstimatedWaitTimeMinutes(), 33);
        assertEquals(conf.getStatus(), 202);
    }
    @Test
    public void cardReaderViewDeserializationTest() throws Exception {
        CardReaderFullResponse conf = mapper.readValue(Utils.loadFile("example-card-full-response.json"), CardReaderFullResponse.class);
        assertEquals(conf.getQuality(), CardReaderQuality.MEDIUM);
        assertEquals(conf.getContact().getOrganizations().size(), 1);

    }
    @Test
    public void cardReaderViewAllDeserializationTest() throws Exception {
        CardReaderViewAllResponse conf = mapper.readValue(Utils.loadFile("example-card-view-all-response.json"), CardReaderViewAllResponse.class);
        assertEquals(conf.getTotalPages(), 11);
        assertEquals(conf.getResults().size(), 2);
    }

    @Test
    public void disposableEmailDeserializationTest() throws Exception {
        DisposableEmailResponse r = mapper.readValue(Utils.loadFile("example-email-response.json"), DisposableEmailResponse.class);
        assertEquals(200, r.getStatus());
        assertEquals("unknown", r.getUsernameSubAddressingStatus());
        assertTrue(r.getMessage().contains("likely associated"));
    }

    @Test
    public void nameNormalizerDeserializationTest() throws Exception {
        NameResponse r = mapper.readValue(Utils.loadFile("example-name-normalization-response.json"), NameResponse.class);
        assertEquals("USA", r.getRegion());
        assertEquals("MBA", r.getNameDetails().getSuffixes().get(1));
        assertEquals("Mr.", r.getNameDetails().getPrefixes().get(0));
    }

    @Test
    public void nameParseDeserializationTest() throws Exception {
        NameParseResponse r = mapper.readValue(Utils.loadFile("example-name-parse-response.json"), NameParseResponse.class);
        assertEquals("Smith", r.getResult().getFamilyName());
        assertEquals("USA", r.getRegion());
    }

    @Test
    public void nameDeduceDeserializationTest() throws Exception {
        NameResponse r = mapper.readValue(Utils.loadFile("example-name-deduce-response.json"), NameResponse.class);
        assertEquals("USA", r.getRegion());
        assertEquals("John", r.getNameDetails().getGivenName());
    }

    @Test
    public void nameStatsDeserializationTest() throws Exception {
        NameStatsResponse r = mapper.readValue(Utils.loadFile("example-name-stats-response.json"), NameStatsResponse.class);
        assertEquals(51.2, r.getName().getGivenNameInfo().getMale().getAge().getAgeInfo().getMeanAge(), 0.001);
        assertEquals(1, r.getName().getFamilyInfo().getRank());
        assertEquals("USA", r.getRegion());
    }

    @Test
    public void nameSimilarityDeserializationTest() throws Exception {
        NameSimilarityResponse r = mapper.readValue(Utils.loadFile("example-name-similarity-response.json"), NameSimilarityResponse.class);
        assertEquals(0, r.getResult().getSimMetricsAlgorithmResults().getJaroWinkler().getTimeActual(), 0.001);
        assertTrue("3 ms".equals(r.getResult().getSecondStringAlgorithmResults().getJaroWinkler().getTimeTaken()));
    }

    @Test
    public void locationEnrichmentDeserializationTest() throws Exception {
        LocationEnrichmentResponse r = mapper.readValue(Utils.loadFile("example-location-enrichment-response.json"),LocationEnrichmentResponse.class);
        assertEquals("San Miguel", r.getPossibleLocations().get(1).getCounty());
        assertEquals("ES", r.getPossibleLocations().get(2).getCountry().getCode());
    }

    @Test
    public void locationNormalizationDeserializationTest() throws Exception {
        LocationNormalizationResponse r = mapper.readValue(Utils.loadFile("example-location-normalization-response.json"), LocationNormalizationResponse.class);
        assertEquals("Denver", r.getCity());
        assertEquals("US", r.getCountry().getCode());
    }

    @Test
    public void accountStatsDeserializationTest() throws Exception {
        AccountStatsResponse r = mapper.readValue(Utils.loadFile("example-account-stats-response.json"), AccountStatsResponse.class);
        assertEquals(7, r.getMetrics().size());
        assertTrue(r.getPlan().contains("Fictitious"));
    }
}
