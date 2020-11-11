// Title Plugin
// Source: https://github.com/wout/svg.js/issues/147
//
// // example usage
// var draw = SVG('drawing')
// var group = draw.group()
// group.title('This is a pink square.')
// group.rect(100,100).fill('#f06')

SVG.Title = SVG.invent({
    create: 'title',
    inherit: SVG.Element,
    extend: {
       text: function(text) {
            while (this.node.firstChild)
                this.node.removeChild(this.node.firstChild)
            this.node.appendChild(document.createTextNode(text))
            return this
        }
    },
    construct: {
        title: function(text) {
            return this.put(new SVG.Title).text(text)
        }
    }
})
