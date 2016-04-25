describe("i18n library", function() {
  it("prettifies time periods in the past", function() {
    expect(i18n.getAgeString(new Date())).toContain("ago");
  });
});