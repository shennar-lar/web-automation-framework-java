Feature: Info panel

  Scenario: INTERNSHIP_110425_verifyAlbumNameAndCover
    Given the user is authenticated in Koel
    When the user plays the album test song and opens INFO to the ALBUM tab
    Then the album name matches the expected album
    And the album cover is loaded

  Scenario: INTERNSHIP_110427_verifyArtistName
    Given the user is authenticated in Koel
    When the user plays the artist test song and opens INFO to the ARTIST tab
    Then the artist name matches the expected artist

  Scenario: INTERNSHIP_110428_verifyInfoPanelOpens
    Given the user is authenticated in Koel and a song is playing
    When the user opens INFO
    Then the Info panel is visible

  Scenario: INTERNSHIP_110429_verifyInfoPanelCloses
    Given the user is authenticated in Koel and INFO is open
    When the user closes INFO
    Then the Info panel is not visible

  Scenario: INTERNSHIP_110432_verifyMissingLyrics
    Given the user is authenticated in Koel
    When the user plays the song with missing lyrics and opens INFO to LYRICS
    Then the displayed lyrics match the expected missing-lyrics message

  Scenario: INTERNSHIP_110433_verifyUnknownAlbumInformation
    Given the user is authenticated in Koel
    When the user plays a song with unknown album information and opens INFO to ALBUM
    Then the album panel displays the expected unknown-album value
