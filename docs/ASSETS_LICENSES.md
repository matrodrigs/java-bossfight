# Assets e licenças

Este build não copia sprites, músicas, efeitos, logos, fontes ou arquivos oficiais de Cuphead.

- `sprites/ui/menu_background.png`: imagem original gerada com OpenAI imagegen para uso como menu 16:9 em estilo de desenho animado teatral dos anos 1930, com palco, cortinas, jardim e botões em branco.
- `sprites/ui/end_victory_background.png`: imagem original gerada com OpenAI imagegen para uso como fundo da tela de vitória, com relógio de bolso sobre fundo escuro.
- `sprites/ui/end_defeat_background.png`: imagem original gerada com OpenAI imagegen para uso como fundo da tela de derrota, com relógio de bolso quebrado sobre fundo escuro.
- `sprites/ui/player_hp_box.png`: imagem original fornecida pelo usuário para uso como caixa de HP do jogador.
- `sprites/ui/special_clock.png`: imagem original fornecida pelo usuário para uso como indicador de carregamento do ataque especial.
- `sprites/boss/flower_boss_sheet.png`: sprite sheet original 4x2 gerada com OpenAI imagegen e preparada localmente para PNG transparente. Contém poses de introdução, idle, roar, ataques e hurt/defeated.
- `sprites/player/clock_player_sheet.png`: sprite sheet original gerada com OpenAI imagegen para o personagem relógio, com poses de idle, andar, pulo, dano, tiro, tiro andando, tiro pulando e especial.
- `sprites/projectiles/player_pea.png`, `player_special.png`, `boss_seed.png`, `boss_acorn.png`, `boss_pollen.png`, `boss_thorn.png`, `boss_petal_bomb.png`, `impact_puff.png`: sprites finais de projéteis e impacto, derivados de gerações originais e recortados para uso em runtime.
- `sprites/background/floral_vintage/**/*.png`: camadas finais de cenário floral vintage, geradas com OpenAI imagegen e recortadas localmente para montagem em parallax.
- `audio/voice/narrator_intro.wav`: locução gerada pelo Google AI Studio para a abertura da luta, sincronizada com os textos READY?/GO!.
- `audio/voice/narrator_knockout.wav`: locução gerada pelo Google AI Studio para o encerramento da luta.
- `audio/music/menu_theme.mp3`, `boss_fight_theme.mp3`, `victory_theme.mp3` e `defeat_theme.mp3`: trilhas sonoras obtidas no site Pixabay.
- Demais efeitos sonoros: sintetizados proceduralmente em runtime pelo `AudioManager`.
- Fontes/texto: texturas geradas localmente em runtime a partir de fontes do sistema/Java2D para evitar o visual borrado do `BitmapFont` padrão.

Arquivos intermediários de geração, sheets brutas, previews locais e scripts temporários de recorte não são embarcados no build final. Nenhum asset oficial de Cuphead foi copiado para o projeto.
