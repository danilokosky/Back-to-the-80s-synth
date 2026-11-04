# Enquadramento Modular por Áreas de Interesse e Magnetismo Suave de Rolagem

Este plano define o ajuste de enquadramento exato da **Área de Interesse 1** (*Cabeçalho + Faders + Barra de Voicing*) em telas widescreen e o sistema de **Magnetismo Suave nas Extremidades** (*Soft Magnetic Edge Snap*) ao deslizar entre o painel superior do sintetizador e a área inferior de performance (*Voicing + Arpejador + Teclado*), tanto no aplicativo Android nativo quanto na versão Web.

## User Review & Critical Decisions

> [!IMPORTANT]
> As decisões abaixo refletem suas respostas e estabelecem o comportamento estático e magnético do layout em todas as proporções de tela:

- **Decisão Confirmada 1 — Enquadramento 100% Exato da Área de Interesse 1 em Widescreen**: Na abertura do aplicativo (e no topo da página), o quadro visível em telas widescreen encaixa **exatamente** o **Cabeçalho Geral**, a **Seção de Faders** e a **Barra de Seleção de Voicing** na borda inferior da tela — sem cortar os botões da barra de Voicing e sem deixar sobrar fragmentos visuais da seção inferior (*Arpejador / Teclado*).
- **Decisão Confirmada 2 — Proporção Estática + Elasticidade Exclusiva dos Faders (Máx. +20%)**: O ajuste de altura para fechar o quadro inicial ocorre **exclusivamente** pelo alongamento vertical da seção dos faders entre `0%` e no máximo `+20%` (`1.00x` a `1.20x`). Todos os demais componentes (*Cabeçalho, Barras Rainbow, Barra de Rolagem, Barra de Voicing, Arpejador e Teclado*) mantêm sua proporção estática alinhada, sem sofrer alterações de escala quando o usuário abre ou recolhe o *Arpejador*, *Presets* ou *Teclado*.
- **Decisão Confirmada 3 — Controle Livre de Rolagem com Magnetismo Suave no Topo e no Fim da Tela**: Os botões `ARP`, `PRESETS` e `TECLADO` apenas abrem/fecham suas gavetas sem forçar saltos bruscos de tela. O usuário tem controle livre ao deslizar a tela verticalmente e, ao soltar o toque próximo ao **topo** (Área de Interesse 1) ou próximo ao **fim da tela / área inferior** (Área de Interesse 2), a tela aplica uma atração magnética suave (*soft ease-out snap*) para alinhar o quadro perfeitamente.
- **Decisão Confirmada 4 — Comportamento em Telas Altas (Tablets / Samsung DeX / Monitores)**: Quando a altura da janela ultrapassar o limite máximo de `+20%` de alongamento dos faders, os faders estacionam em `+20%` (`1.20x`) para preservar a estética original do hardware, e o espaço vertical excedente passa a exibir naturalmente o *Arpejador* e o *Teclado* abaixo da barra de Voicing.

---

## 1. Overview & Core Concept

- **O Que Faz**: Transforma o chassi do sintetizador em duas **Áreas de Interesse Magnéticas** com alturas matematicamente determinadas:
  1. **Área de Interesse Superior (Sintetizador Principal)**: Do topo do **Cabeçalho Geral**, passando pelos **Faders (LFO, DCO/VCO, HPF, VCF, VCA, ENV, Chorus, Delay, Reverb)**, até a base da **Barra de Voicing**.
  2. **Área de Interesse Inferior (Performance & Teclado)**: Enquadra o **fim da página** com o **Teclado Polifônico**, o **Arpejador** e a **Barra de Voicing** acessíveis confortavelmente na região superior/central de alcance das mãos.
- **Valor Principal**: Elimina a inconsistência visual entre os estados recolhido e expandido do *Arpejador/Teclado* (onde antes a barra de Voicing ficava cortada ou exibia sobras da seção seguinte) e proporciona uma sensação tátil de equipamento físico ao deslizar entre os dois planos.

---

## 2. User Experience & Visual Design

- **Fluxo de Enquadramento e Rolagem Magnética**:
  1. **Abertura em Widescreen (Smartphone Paisagem / Janela Widescreen)**:
     - Os componentes fixos (*Cabeçalho*, *Listras Rainbow*, *Barra de Rolagem Horizontal* e *Barra de Voicing*) possuem alturas estáticas precisas.
     - A seção dos **Faders** calcula a diferença exata para completar `100%` da altura da janela, alongando-se apenas dentro da faixa permitida de `1.00x` a `1.20x` (máximo de `+20%`).
     - Resultado: A base da **Barra de Voicing** encosta exatamente no limite inferior da tela, sem cortes e sem mostrar bordas ou sobras do *Arpejador* ou *Teclado* abaixo.
  2. **Estado Totalmente Recolhido (`PRESETS`, `ARP` e `TECLADO` fechados)**:
     - Quando todas as gavetas inferiores estão recolhidas, o contêiner inferior fica completamente oculto (sem bordas vazias ou espaçamentos extras), mantendo o quadro travado em `100%` na **Área de Interesse 1**.
  3. **Deslize Vertical com Magnetismo Suave nas Extremidades**:
     - Durante o arrasto com o dedo ou mouse/trackpad, a rolagem acompanha o movimento livremente sem travas artificiais.
     - Ao finalizar o gesto de deslize (*touch release / scroll idle*):
       - **Zona Magnética Superior**: Se a posição de rolagem estiver próxima ao topo (dentro da zona de atração superior), a visualização desliza suavemente (`animateScrollTo(0)`) para cravar o topo do **Cabeçalho** e a base da **Barra de Voicing** na tela.
       - **Zona Magnética Inferior**: Se a posição de rolagem se aproximar do fim da página (ou do âncora da **Barra de Voicing + Arpejador + Teclado**), a visualização desliza suavemente até encaixar o fundo da página / Área de Interesse 2 sem deixar elementos cortados pela metade.

---

## 3. Key Product Decisions & Trade-Offs

- **Decisão 1: Desacoplar a Altura dos Faders do Estado das Gavetas (`ARP` / `PRESETS` / `TECLADO`)**
  - *Abordagem Escolhida*: Calcular o fator de alongamento dos faders (`1.00x .. 1.20x`) unicamente a partir da altura da tela (`viewportHeight`) menos a soma das alturas estáticas da **Área de Interesse 1** (`Cabeçalho + Rainbow + Scrollbar + Rainbow + Barra de Voicing`).
  - *Por Quê*: Anteriormente, abrir ou fechar o *Arpejador* ou o *Teclado* alterava a variável de cálculo dos faders, fazendo com que a altura dos faders mudasse e deslocasse a barra de Voicing para fora da tela ou revelasse pedaços cortados da seção inferior. Com o cálculo estático da Área de Interesse 1, a primeira tela permanece geometricamente idêntica esteja o teclado aberto ou fechado lá embaixo.
- **Decisão 2: Magnetismo Suave Baseado no Término do Gesto (`Scroll Idle / Fling Settled`)**
  - *Abordagem Escolhida*: Monitorar o estado `isScrollInProgress` (no Compose Android) e o término do evento `scroll` com *debounce* curto (no Web), acionando uma animação suave (`tween` de ~220ms) apenas quando o usuário solta o toque dentro das zonas magnéticas próximas ao topo ou ao fim da tela.
  - *Por Quê*: Garante controle total ao usuário durante o movimento de rolagem, sem "puxar" a tela contra o dedo enquanto ele ainda está deslizando, mas entregando o encaixe magnético preciso assim que ele aproxima a cena do topo ou do fundo.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

```
┌────────────────────────────────────────────────────────────────────────────┐
│                   VIEWPORT CONTAINER (Altura da Janela: H)                 │
├────────────────────────────────────────────────────────────────────────────┤
│  [ZONA MAGNÉTICA 1: TOPO -> scroll = 0]                                    │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ 1. TopHeaderBar (Altura Estática Fixa: H_header)                     │  │
│  ├──────────────────────────────────────────────────────────────────────┤  │
│  │ 2. Top Rainbow Stripe (H_rainbow)                                    │  │
│  ├──────────────────────────────────────────────────────────────────────┤  │
│  │ 3. SynthRackSectionRow (Altura Elástica Controlada)                  │  │
│  │    • H_rack_ideal = H - (H_header + 2*H_rainbow + H_scroll + H_voic) │  │
│  │    • faderElongation = clamp(H_rack_ideal / H_rack_base, 1.00, 1.20) │  │
│  │    • H_rack_final = H_rack_base * faderElongation                    │  │
│  ├──────────────────────────────────────────────────────────────────────┤  │
│  │ 4. Horizontal Rack Scrollbar (H_scroll) + Bottom Rainbow (H_rainbow) │  │
│  ├──────────────────────────────────────────────────────────────────────┤  │
│  │ 5. VoicingPresetBar (Altura Estática Fixa: H_voic)                   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│  ═══════ [LIMITE EXATO DE 100% DA TELA EM WIDESCREEN (Sem Sobras)] ═══════ │
│                                                                            │
│  [ZONA MAGNÉTICA 2: FUNDO -> scroll ≈ maxScroll / Âncora Voicing+Teclado]  │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ 6. Gavetas Modulares Inferiores (Renderizadas só quando ativas)      │  │
│  │    • VoicingTimbresInlineBrowser (quando PRESETS ativo)              │  │
│  │    • ArpeggiatorKeyboardBar      (quando ARP ativo)                  │  │
│  │    • MultiTouchPolyphonicKeyboard (quando TECLADO ativo)             │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────────┘
```

### Mapeamento de Estado e Comportamento Interativo
- **Cálculo Exato da Área de Interesse 1**:
  - As alturas estáticas (`H_header`, `H_rainbow`, `H_scroll`, `H_voic`) são fixadas sem dependências dinâmicas das gavetas inferiores.
  - Em telas widescreen onde `H - H_estatico` está entre `100%` e `120%` da altura base do rack, `H_rack_final` preenche exatamente `H - H_estatico`, fazendo com que a soma `H_estatico + H_rack_final` seja exatamente igual a `H`.
  - Caso todas as três gavetas inferiores (`PRESETS`, `ARP`, `TECLADO`) estejam desativadas, o bloco inferior não é renderizado no layout, eliminando qualquer borda residual abaixo da barra de Voicing.
- **Controlador de Magnetismo nas Extremidades (`Soft Edge Snap`)**:
  - Observa `mainVerticalScroll.isScrollInProgress` (Android) e o término do deslize vertical (Web).
  - Quando o gesto termina próximo ao topo (`0 < scrollValue <= snapTopThreshold`), executa `animateScrollTo(0)` com curva suave.
  - Quando o gesto termina próximo ao fim da tela (`maxScroll - snapBottomThreshold <= scrollValue < maxScroll`) ou próximo ao enquadramento da Área de Interesse 2 (`VoicingPresetBar` no topo + `Arpejador` + `Teclado` encaixados na base), executa `animateScrollTo(targetBottomSnap)` suavemente.
