const frisby = require('frisby');

describe("Customer Management REST API:", function () {

    const BASE_URL = "http://localhost:8082";
    var tenantId = 'acme';

    var originalTimeout;
    var accountsBefore = [];
    var contactsBefore = [];
    var contacts = [];
    var contact = {
        firstName: 'Fred',
        lastName: 'Flintstone',
        email: 'fred@slaterock.com',
        customFields: {
            dateOfBirth: '01/01/1970'
        }
    };
    var account = {
        name: 'Bedrock Slate and Gravel'
    };
    var note = {
        author: 'boss@slaterock.com',
        content: 'Fred is a great worker'
    }
    var doc = {
        author: 'boss@slaterock.com',
        name: 'Annual review',
        url: 'http://slaterock.com/reviews/Fred.odt',
        favorite: true
    }
    var activity = {
        content: 'Annual review',
        type: 'TEST'
    }
    describe("GET baseline data", function () {
        it("for existing contacts and accounts", function (done) {
            frisby.get(`${BASE_URL}/${tenantId}/contacts/`)
                .then(function (response) {
                    console.warn(`Baseline contacts: ${response.json.length}`);
                    expect(response.status).toBe(200);
                    contactsBefore = response.json;

                    frisby.get(`${BASE_URL}/${tenantId}/accounts/`)
                        .then(function (response) {
                            console.warn(`Baseline accounts: ${response.json.length}`);
                            expect(response.status).toBe(200);
                            accountsBefore = response.json;
                        })
                        .done(done);
                });
        });
    });
});
